package spendings.model.propdist

import play.api.libs.json._

case class DateP(id: Option[Int], tokenFk: Int,
                   count: Int, total: Int)

object DateP {
  implicit val amountPReads = Json.reads[DateP]

  implicit val amountPWrites = Json.writes[DateP]

  val tupled = (this.apply _).tupled
}
