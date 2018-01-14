package spendings.model.propdist

import play.api.libs.json._

import spendings.model._

case class DescriptionP(id: Option[Int], tokenFk: Int, descriptionFk: Int,
                        count: Int, total: Int) extends HasCopy[DescriptionP] {
  def cpy(i: Option[Int]) = this.copy(id = i)
}

object DescriptionP {
  implicit val descriptionPReads = Json.reads[DescriptionP]

  implicit val descriptionPWrites = Json.writes[DescriptionP]

  val tupled = (this.apply _).tupled
}
