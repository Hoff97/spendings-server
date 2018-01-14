package spendings.model.propdist

import play.api.libs.json._

import spendings.model._

case class DateP(id: Option[Int], tokenFk: Int,
                 count: Int, total: Int) extends HasCopy[DateP] {
  def cpy(i: Option[Int]) = this.copy(id = i)
}

object DateP {
  implicit val amountPReads = Json.reads[DateP]

  implicit val amountPWrites = Json.writes[DateP]

  val tupled = (this.apply _).tupled
}
