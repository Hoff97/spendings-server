package spendings.model.propdist

import play.api.libs.json._

case class DescriptionP(id: Option[Int], tokenFk: Int, descriptionFk: Int,
                        count: Int, total: Int)

object DescriptionP {
  implicit val descriptionPReads = Json.reads[DescriptionP]

  implicit val descriptionPWrites = Json.writes[DescriptionP]

  val tupled = (this.apply _).tupled
}
