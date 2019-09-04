package io.scalaworld.hiking.util

import java.time.Clock

import io.scalaworld.hiking.config.Config

trait BaseModule {
  def idGenerator: IdGenerator
  def clock: Clock
  def config: Config
}
