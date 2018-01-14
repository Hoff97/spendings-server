package spendings.db.propdist

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape.proveShapeOf
import spendings.model.propdist._
import spendings.model._

class AmountPTable(tag: Tag) extends Table[AmountP](tag, "amount_tokenp") with HasID[AmountP] {
  def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
  def tokenFk = column[Int]("token_fk")

  def count = column[Int]("count")
  def total = column[Int]("total")

  def * = (id.?,tokenFk,count,total) <> (AmountP.tupled, AmountP.unapply)
}

object AmountPTable {
  val amountP = TableQuery[AmountPTable]
}
