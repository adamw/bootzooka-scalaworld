package io.scalaworld.hiking.email.sender

import io.scalaworld.hiking.email.EmailData
import monix.eval.Task

trait EmailSender {
  def apply(email: EmailData): Task[Unit]
}
