package spendings.model.propdist

import play.api.libs.json._

import spendings.model._

case class AmountP(id: Option[Int], tokenFk: Int,
                   count: Int, total: Int) extends HasCopy[AmountP] {
  def cpy(i: Option[Int]) = this.copy(id = i)
}

object AmountP {
  implicit val amountPReads = Json.reads[AmountP]

  implicit val amountPWrites = Json.writes[AmountP]

  val tupled = (this.apply _).tupled
}
