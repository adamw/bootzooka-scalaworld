package io.scalaworld.hiking.metrics

import io.scalaworld.hiking.http.{Error_OUT, Http}
import io.scalaworld.hiking.infrastructure.Json._
import io.scalaworld.hiking.version.BuildInfo
import monix.eval.Task
import tapir.model.StatusCode
import tapir.server.ServerEndpoint

/**
  * Defines an endpoint which exposes the current application version information.
  */
class VersionApi(http: Http) {
  import VersionApi._
  import http._

  val versionEndpoint: ServerEndpoint[Unit, (StatusCode, Error_OUT), Version_OUT, Nothing, Task] = baseEndpoint.get
    .in("version")
    .out(jsonBody[Version_OUT])
    .serverLogic { _ =>
      Task.now(Version_OUT(BuildInfo.builtAtString, BuildInfo.lastCommitHash)).toOut
    }
}

object VersionApi {
  case class Version_OUT(buildDate: String, buildSha: String)
}
