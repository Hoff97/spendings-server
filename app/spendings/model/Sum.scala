package spendings.model

import play.api.libs.json._

case class Sum(name: String, sum: Double, average: Double, count: Int)

object Sum {
  implicit val sumReads = Json.reads[Sum]

  implicit val sumWrites = Json.writes[Sum]

  val tupled = (this.apply _).tupled
}
