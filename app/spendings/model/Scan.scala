package spendings.model

import play.api.libs.json._
import java.sql.Timestamp
import spendings.util.DateTime._

case class Scan(id: Option[Int], result: String) extends HasCopy[Scan] {
  def cpy(i: Option[Int]) = this.copy(id = i)
}

object Scan {
  implicit val scanReads = Json.reads[Scan]

  implicit val scanWrites = Json.writes[Scan]

  val tupled = (this.apply _).tupled
}
