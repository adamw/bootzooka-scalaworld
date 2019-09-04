package io.scalaworld.hiking

import io.scalaworld.hiking.config.ConfigModule
import io.scalaworld.hiking.infrastructure.DB

/**
  * Initialised resources needed by the application to start.
  */
trait InitModule extends ConfigModule {
  lazy val db: DB = new DB(config.db)
}
