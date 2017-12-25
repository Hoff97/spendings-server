package spendings.db

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape.proveShapeOf
import spendings.model._

class UserTable(tag: Tag) extends Table[User](tag, "users") with HasID[User] {
  def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
  def name = column[String]("name",O.Unique)

  def pwSalt = column[String]("pwsalt")
  def pwHash = column[String]("pwhash")
  def pwHasher = column[String]("pwhasher")

  def providerId = column[String]("provider_id")
  def providerKey = column[String]("provider_key")

  def email = column[String]("email")

  def * = (id.?,name,email,pwHash.?, pwSalt.?, pwHasher.? ,providerId, providerKey) <> (User.tupled, User.unapply)
}

object UserTable {
  val user = TableQuery[UserTable]
}
