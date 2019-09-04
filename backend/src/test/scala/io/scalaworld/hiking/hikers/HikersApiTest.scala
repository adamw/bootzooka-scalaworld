package io.scalaworld.hiking.hikers

import java.time.Clock

import io.scalaworld.hiking.MainModule
import io.scalaworld.hiking.config.Config
import io.scalaworld.hiking.infrastructure.Doobie.Transactor
import io.scalaworld.hiking.test.{BaseTest, Requests, TestConfig, TestEmbeddedPostgres}
import io.scalaworld.hiking.user.UserApi.Login_IN
import monix.eval.Task
import org.http4s.Request
import org.http4s._
import io.scalaworld.hiking.infrastructure.Json._

import tapir.client.sttp._

class HikersApiTest extends BaseTest with TestEmbeddedPostgres {
  lazy val testModules: MainModule = new MainModule {
    override def xa: Transactor[Task] = currentDb.xa
    override lazy val config: Config = TestConfig
    override lazy val clock: Clock = testClock
  }

  val requests = new Requests(testModules)
  import requests._

  it should "create a new hiker" in {
    // given
    val RegisteredUser(_, _ , _, apiKey) = newRegisteredUsed()

    // when
    val request = Request[Task](method = POST, uri = uri"/hikers")
      .withEntity(CreateHiker_IN("keswick to penrith"))

    val response = modules.httpApi.mainRoutes(authorizedRequest(apiKey, request)).unwrap

    // then
    val result = response.attemptAs[String](EntityDecoder.text).value.unwrap
    result shouldBe Right("ok")
  }
}
