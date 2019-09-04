package io.scalaworld.hiking.passwordreset

import io.scalaworld.hiking.email.{EmailScheduler, EmailTemplates}
import io.scalaworld.hiking.http.Http
import io.scalaworld.hiking.security.Auth
import io.scalaworld.hiking.user.UserModel
import io.scalaworld.hiking.util.BaseModule
import doobie.util.transactor.Transactor
import monix.eval.Task

trait PasswordResetModule extends BaseModule {
  lazy val passwordResetCodeModel = new PasswordResetCodeModel
  lazy val passwordResetService =
    new PasswordResetService(
      userModel,
      passwordResetCodeModel,
      emailScheduler,
      emailTemplates,
      passwordResetCodeAuth,
      idGenerator,
      config.passwordReset,
      clock,
      xa
    )
  lazy val passwordResetApi = new PasswordResetApi(http, passwordResetService, xa)

  def userModel: UserModel
  def http: Http
  def passwordResetCodeAuth: Auth[PasswordResetCode]
  def emailScheduler: EmailScheduler
  def emailTemplates: EmailTemplates
  def xa: Transactor[Task]
}
