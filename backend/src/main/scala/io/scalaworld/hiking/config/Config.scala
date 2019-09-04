package io.scalaworld.hiking.config

import io.scalaworld.hiking.email.EmailConfig
import io.scalaworld.hiking.http.HttpConfig
import io.scalaworld.hiking.infrastructure.DBConfig
import io.scalaworld.hiking.passwordreset.PasswordResetConfig
import io.scalaworld.hiking.user.UserConfig

/**
  * Maps to the `application.conf` file. Configuration for all modules of the application.
  */
case class Config(db: DBConfig, api: HttpConfig, email: EmailConfig, passwordReset: PasswordResetConfig, user: UserConfig)
