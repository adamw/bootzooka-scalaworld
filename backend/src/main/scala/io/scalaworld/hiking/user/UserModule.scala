package io.scalaworld.hiking.user

import io.scalaworld.hiking.email.{EmailScheduler, EmailTemplates}
import io.scalaworld.hiking.http.Http
import io.scalaworld.hiking.security.{ApiKey, ApiKeyService, Auth}
import io.scalaworld.hiking.util.BaseModule
import doobie.util.transactor.Transactor
import monix.eval.Task

trait UserModule extends BaseModule {
  lazy val userModel = new UserModel
  lazy val userApi = new UserApi(http, apiKeyAuth, userService, xa)
  lazy val userService = new UserService(userModel, emailScheduler, emailTemplates, apiKeyService, idGenerator, clock, config.user)

  def http: Http
  def apiKeyAuth: Auth[ApiKey]
  def emailScheduler: EmailScheduler
  def emailTemplates: EmailTemplates
  def apiKeyService: ApiKeyService
  def xa: Transactor[Task]
}
