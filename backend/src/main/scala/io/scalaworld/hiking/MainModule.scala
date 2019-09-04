package io.scalaworld.hiking

import java.time.Clock

import cats.data.NonEmptyList
import io.scalaworld.hiking.email.EmailModule
import io.scalaworld.hiking.http.{Http, HttpApi}
import io.scalaworld.hiking.infrastructure.InfrastructureModule
import io.scalaworld.hiking.metrics.MetricsModule
import io.scalaworld.hiking.passwordreset.PasswordResetModule
import io.scalaworld.hiking.security.SecurityModule
import io.scalaworld.hiking.user.UserModule
import io.scalaworld.hiking.util.{DefaultIdGenerator, IdGenerator, ServerEndpoints}
import monix.eval.Task
import cats.implicits._
import io.scalaworld.hiking.hikers.HikersModule

/**
  * Main application module. Depends on resources initalised in [[InitModule]].
  */
trait MainModule
    extends SecurityModule
    with EmailModule
    with UserModule
    with PasswordResetModule
    with MetricsModule
    with InfrastructureModule
    with HikersModule {

  override lazy val idGenerator: IdGenerator = DefaultIdGenerator
  override lazy val clock: Clock = Clock.systemUTC()

  lazy val http: Http = new Http()

  private lazy val endpoints: ServerEndpoints = userApi.endpoints concatNel passwordResetApi.endpoints concatNel hikersApi.endpoints
  private lazy val adminEndpoints: ServerEndpoints = NonEmptyList.of(metricsApi.metricsEndpoint, versionApi.versionEndpoint)

  lazy val httpApi: HttpApi = new HttpApi(http, endpoints, adminEndpoints, collectorRegistry, config.api)

  lazy val startBackgroundProcesses: Task[Unit] = emailService.startProcesses().void
}
