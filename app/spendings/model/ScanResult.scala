package spendings.model

import play.api.libs.json._
import java.sql.Timestamp
import spendings.model.ProbValue._
import spendings.util.DateTime._

case class ScanResult(price: List[BigDecimal], category: List[Category],
                      description: List[String], date: List[Timestamp])

object ScanResult {
  implicit val scanResultReads = Json.reads[ScanResult]

  implicit val scanResultWrites = Json.writes[ScanResult]

  val tupled = (this.apply _).tupled
}
