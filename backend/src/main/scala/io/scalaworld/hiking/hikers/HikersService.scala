package io.scalaworld.hiking.hikers

import com.softwaremill.tagging.@@
import com.typesafe.scalalogging.StrictLogging
import io.scalaworld.hiking.email.EmailData
import io.scalaworld.hiking.email.sender.EmailSender
import io.scalaworld.hiking.user.User
import io.scalaworld.hiking.util.{Id, IdGenerator}
import monix.eval.Task
import io.scalaworld.hiking.infrastructure.Doobie._

class HikersService(hikersModel: HikersModel, emailSender: EmailSender, xa: Transactor[Task], idGenerator: IdGenerator) extends StrictLogging {
  def newHiker(userId: Id @@ User, routeName: String): Task[Unit] = {
    val dbOp: ConnectionIO[Unit] = hikersModel.insert(Hiker(idGenerator.nextId(), userId, routeName))
    val dbResult: Task[Unit] = dbOp.transact(xa)
    val sendEmail: Task[Unit] = emailSender(EmailData("admin@scalaworld.io", "New hiker!", s"New hiker: $userId"))

    logger.info(s"new hiker on $routeName")

    sendEmail.flatMap(_ => dbResult)
  }
}
