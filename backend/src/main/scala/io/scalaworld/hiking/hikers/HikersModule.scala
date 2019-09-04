package io.scalaworld.hiking.hikers

import io.scalaworld.hiking.email.sender.EmailSender
import io.scalaworld.hiking.http.Http
import io.scalaworld.hiking.security.{ApiKey, Auth}
import io.scalaworld.hiking.util.IdGenerator
import monix.eval.Task
import io.scalaworld.hiking.infrastructure.Doobie._

trait HikersModule {
  lazy val hikersModel = new HikersModel
  lazy val hikersApi = new HikersApi(http, apiKeyAuth, hikersService)
  lazy val hikersService = new HikersService(hikersModel, emailSender, xa, idGenerator)

  def emailSender: EmailSender
  def http: Http
  def apiKeyAuth: Auth[ApiKey]
  def idGenerator: IdGenerator
  def xa: Transactor[Task]
}
