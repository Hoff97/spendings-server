package spendings.model

import play.api.libs.json._
import java.sql.Timestamp
import spendings.util.DateTime._

case class Spending(id: Option[Int],amount: BigDecimal, description: String,
                    date: Timestamp, categoryFk: Int, userFk: Int) extends HasCopy[Spending] {
  def cpy(i: Option[Int]) = this.copy(id = i)
}

object Spending{
  implicit val spendingReads = Json.reads[Spending]

  implicit val spendingWrites = Json.writes[Spending]

  val tupled = (this.apply _).tupled
}
