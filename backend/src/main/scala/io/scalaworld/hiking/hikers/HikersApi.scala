package io.scalaworld.hiking.hikers

import cats.data.NonEmptyList
import io.scalaworld.hiking.http.{Error_OUT, Http}
import io.scalaworld.hiking.infrastructure.Json._
import io.scalaworld.hiking.security.{ApiKey, Auth}
import io.scalaworld.hiking.util.ServerEndpoints

class HikersApi(http: Http, auth: Auth[ApiKey], hikersService: HikersService) {

  import http._

  val createHikerEndpoint = secureEndpoint
    .in("hikers")
    .post
    .in(jsonBody[CreateHiker_IN])
    .out(stringBody)
    .serverLogic {
      case (apiKeyId, hikerIn) =>
        (for {
          userId <- auth(apiKeyId)
          _ <- hikersService.newHiker(userId, hikerIn.routeName)
        } yield "ok").toOut
    }

  val endpoints: ServerEndpoints = NonEmptyList
    .of(createHikerEndpoint)
    .map(_.tag("hikers"))
}

case class CreateHiker_IN(routeName: String)
