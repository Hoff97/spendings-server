package spendings.model.propdist

import play.api.libs.json._

import spendings.model._

case class CategoryP(id: Option[Int], categoryFk: Int, tokenFk: Int,
                     count: Int, total: Int) extends HasCopy[CategoryP] {
  def cpy(i: Option[Int]) = this.copy(id = i)
}

object CategoryP {
  implicit val categoryPReads = Json.reads[CategoryP]

  implicit val categoryPWrites = Json.writes[CategoryP]

  val tupled = (this.apply _).tupled
}
