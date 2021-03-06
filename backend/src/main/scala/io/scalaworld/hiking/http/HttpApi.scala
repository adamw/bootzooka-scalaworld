package io.scalaworld.hiking.http

import java.util.concurrent.Executors

import cats.effect.ExitCode
import io.scalaworld.hiking.Fail
import io.scalaworld.hiking.infrastructure.CorrelationId
import io.scalaworld.hiking.util.ServerEndpoints
import io.prometheus.client.CollectorRegistry
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.http4s.dsl.Http4sDsl
import org.http4s.metrics.prometheus.Prometheus
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, CORSConfig, Metrics}
import org.http4s.syntax.kleisli._
import org.http4s.server.staticcontent._
import org.http4s.server.staticcontent.ResourceService
import org.http4s.{HttpApp, HttpRoutes, Request, StaticFile}
import tapir.DecodeResult
import tapir.docs.openapi._
import tapir.model.StatusCode
import tapir.openapi.Server
import tapir.openapi.circe.yaml._
import tapir.server.http4s._
import tapir.server.{DecodeFailureHandler, DecodeFailureHandling, ServerDefaults}
import tapir.swagger.http4s.SwaggerHttp4s
import cats.implicits._

import scala.concurrent.ExecutionContext

/**
  * Interprets the endpoint descriptions (defined using tapir) as http4s routes, adding CORS, metrics, api docs
  * and correlation id support.
  *
  * The following endpoints are exposed:
  * - `/api/v1` - the main API
  * - `/api/v1/docs` - swagger UI for the main API
  * - `/admin` - admin API
  */
class HttpApi(
    http: Http,
    endpoints: ServerEndpoints,
    adminEndpoints: ServerEndpoints,
    collectorRegistry: CollectorRegistry,
    config: HttpConfig
) {
  private val apiContextPath = "api/v1"
  private val docsContextPath = s"$apiContextPath/docs"

  lazy val mainRoutes: HttpRoutes[Task] = CorrelationId.setCorrelationIdMiddleware(toRoutes(endpoints))
  private lazy val adminRoutes: HttpRoutes[Task] = toRoutes(adminEndpoints)
  private lazy val docsRoutes: HttpRoutes[Task] = {
    val openapi = endpoints.toList.toOpenAPI("Hiking", "1.0").copy(servers = List(Server(s"/$apiContextPath", None)))
    val yaml = openapi.toYaml
    new SwaggerHttp4s(yaml, docsContextPath).routes[Task]
  }

  private lazy val corsConfig: CORSConfig = CORS.DefaultCORSConfig

  /**
    * A never-ending stream which handles incoming requests.
    */
  lazy val serveRequests: fs2.Stream[Task, ExitCode] = {
    val prometheusHttp4sMetrics = Prometheus[Task](collectorRegistry)
    fs2.Stream
      .eval(prometheusHttp4sMetrics.map(m => Metrics[Task](m)(mainRoutes)))
      .flatMap { monitoredServices =>
        val app: HttpApp[Task] =
          Router(
            s"/$docsContextPath" -> docsRoutes,
            s"/$apiContextPath" -> CORS(monitoredServices, corsConfig),
            "/admin" -> adminRoutes,
            "" -> webappRoutes
          ).orNotFound

        BlazeServerBuilder[Task]
          .bindHttp(config.port, config.host)
          .withHttpApp(app)
          .serve
      }
  }

  /**
    * tapir's Codecs parse inputs - query parameters, JSON bodies, headers - to their desired types. This might fail,
    * and then a decode failure is returned, instead of a value. How such a failure is handled can be customised.
    *
    * We want to return responses in the same JSON format (corresponding to [[Error_OUT]]) as other errors returned
    * during normal request processing.
    *
    * We use the default behavior of tapir (`ServerDefaults.decodeFailureHandlerUsingResponse`), customising the format
    * used for returning errors (`http.failOutput`). This will cause `400 Bad Request` to be returned in most cases.
    *
    * Additionally, if the error thrown is a `Fail` we might get additional information, such as a custom status
    * code, by translating it using the `http.exceptionToErrorOut` method and using that to create the response.
    */
  private val decodeFailureHandler: DecodeFailureHandler[Request[Task]] = {
    // if an exception is thrown when decoding an input, and the exception is a Fail, responding basing on the Fail
    case (_, _, DecodeResult.Error(_, f: Fail)) => DecodeFailureHandling.response(http.failOutput)(http.exceptionToErrorOut(f))
    // otherwise, converting the decode input failure into a response using tapir's defaults
    case (req, input, failure) =>
      def failResponse(code: StatusCode, msg: String): DecodeFailureHandling =
        DecodeFailureHandling.response(http.failOutput)((code, Error_OUT(msg)))
      ServerDefaults.decodeFailureHandlerUsingResponse(failResponse, badRequestOnPathFailureIfPathShapeMatches = false)(req, input, failure)
  }

  /**
    * Interprets the given endpoint descriptions as http4s routes
    */
  private def toRoutes(es: ServerEndpoints): HttpRoutes[Task] = {
    implicit val serverOptions: Http4sServerOptions[Task] = Http4sServerOptions
      .default[Task]
      .copy(
        decodeFailureHandler = decodeFailureHandler
      )
    es.toList.toRoutes
  }

  /**
    * Serves the webapp resources (html, js, css files), from the /webapp directory on the classpath.
    */
  private lazy val webappRoutes: HttpRoutes[Task] = {
    val dsl = Http4sDsl[Task]
    import dsl._
    val blockingEc = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
    val rootRoute = HttpRoutes.of[Task] {
      case request @ GET -> Root =>
        StaticFile.fromResource("/webapp/index.html", blockingEc, Some(request)).getOrElseF(NotFound())
    }
    val resourcesRoutes = resourceService[Task](ResourceService.Config("/webapp", blockingEc))
    rootRoute <+> resourcesRoutes
  }
}
