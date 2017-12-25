package spendings.model

import java.sql.Timestamp
import play.api.libs.json._
import spendings.util.DateTime._

case class Search(search: Option[String], category: Option[Int], sort:Option[String], sortDir: Boolean, from: Option[java.sql.Timestamp], to: Option[java.sql.Timestamp])

object Search {
  implicit val searchReads = Json.reads[Search]

  implicit val searchWrites = Json.writes[Search]

  val tupled = (this.apply _).tupled
}
