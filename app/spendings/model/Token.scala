package spendings.model

import play.api.libs.json._

case class Token(id: Option[Int], text: String) extends HasCopy[Token]{
  def cpy(i: Option[Int]) = this.copy(id = i)
}

object Token {
  implicit val tokenReads = Json.reads[Token]

  implicit val tokenWrites = Json.writes[Token]

  val tupled = (this.apply _).tupled
}
