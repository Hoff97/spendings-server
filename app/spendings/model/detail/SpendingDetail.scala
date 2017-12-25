package spendings.model.detail

import play.api.libs.json._
import spendings.model._
import spendings.db._
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext
import java.sql.Timestamp
import spendings.util.DateTime._

case class SpendingDetail(id: Option[Int],amount: BigDecimal, description: String,
                          date: Timestamp, category: Category) {}

object SpendingDetail {
  implicit val spendingDetailReads = Json.reads[SpendingDetail]
  implicit val spendingDetailWrites = Json.writes[SpendingDetail]

  val tupled = (this.apply _).tupled

  implicit class SpendingDetailQuery(q: Query[SpendingTable, Spending, Seq]) {
    def detailed(implicit ec: ExecutionContext) = q
        .join(CategoryTable.category).on(_.categoryFk === _.id)
        .result.map(_.map {
          case (spending,category) => SpendingDetail(spending.id, spending.amount,spending.description, spending.date, category)
      })
  }
}
