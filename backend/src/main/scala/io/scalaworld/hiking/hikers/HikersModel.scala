package io.scalaworld.hiking.hikers

import com.softwaremill.tagging.@@
import io.scalaworld.hiking.user.User
import io.scalaworld.hiking.util.Id
import io.scalaworld.hiking.infrastructure.Doobie._

class HikersModel {
  def insert(hiker: Hiker): ConnectionIO[Unit] = {
    sql"""INSERT INTO hikers (id, user_id, route_name)
         |VALUES (${hiker.id}, ${hiker.userId}, ${hiker.routeName})""".stripMargin.update.run.map(_ => ())
  }
}

case class Hiker(id: Id @@ Hiker, userId: Id @@ User, routeName: String)


