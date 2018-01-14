package spendings.model.propdist

import play.api.libs.json._

case class AmountP(id: Option[Int], tokenFk: Int,
                        count: Int, total: Int)

object AmountP {
  implicit val amountPReads = Json.reads[AmountP]

  implicit val amountPWrites = Json.writes[AmountP]

  val tupled = (this.apply _).tupled
}
