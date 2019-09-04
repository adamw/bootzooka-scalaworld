package io.scalaworld.hiking.email.sender

import io.scalaworld.hiking.email.EmailData
import io.scalaworld.hiking.test.BaseTest
import monix.execution.Scheduler.Implicits.global

class DummyEmailSenderTest extends BaseTest {
  it should "send scheduled email" in {
    DummyEmailSender(EmailData("test@sml.com", "subject", "content")).runSyncUnsafe()
    DummyEmailSender.findSentEmail("test@sml.com", "subject") shouldBe 'defined
  }
}
