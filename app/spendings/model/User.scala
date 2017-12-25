package spendings.model

import play.api.libs.json._
import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }

case class User(id: Option[Int], name: String, email: String, pwHash: Option[String],
                pwSalt: Option[String], pwHasher: Option[String], providerID: String, providerKey: String) extends HasCopy[User] with Identity {
  def cpy(i: Option[Int]) = this.copy(id = i)

  val loginInfo = LoginInfo(providerID,providerKey)
}

object User {
  implicit val userReads = Json.reads[User]

  implicit val userWrites = Json.writes[User]

  val tupled = (this.apply _).tupled
}
