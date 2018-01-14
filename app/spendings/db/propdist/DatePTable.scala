package spendings.db.propdist

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape.proveShapeOf
import spendings.model.propdist._
import spendings.model._

class DatePTable(tag: Tag) extends Table[DateP](tag, "date_tokenp") with HasID[DateP] {
  def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
  def tokenFk = column[Int]("token_fk")

  def count = column[Int]("count")
  def total = column[Int]("total")

  def * = (id.?,tokenFk,count,total) <> (DateP.tupled, DateP.unapply)
}

object DatePTable {
  val dateP = TableQuery[DatePTable]
}
