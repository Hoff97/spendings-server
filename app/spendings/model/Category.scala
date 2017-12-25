package spendings.model

import play.api.libs.json._

case class Category(id: Option[Int], name: String, parent: Int) extends HasCopy[Category]{
  def cpy(i: Option[Int]) = this.copy(id = i)
}

object Category {
  implicit val categoryReads = Json.reads[Category]

  implicit val categoryWrites = Json.writes[Category]

  val tupled = (this.apply _).tupled
}
