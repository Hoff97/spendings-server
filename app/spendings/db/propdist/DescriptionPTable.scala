package spendings.db.propdist

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape.proveShapeOf
import spendings.model.propdist._
import spendings.model._

class DescriptionPTable(tag: Tag) extends Table[DescriptionP](tag, "description_tokenp") with HasID[DescriptionP] {
  def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
  def descriptionFk = column[Int]("description_fk")
  def tokenFk = column[Int]("token_fk")

  def count = column[Int]("count")
  def total = column[Int]("total")

  def * = (id.?,descriptionFk,tokenFk,count,total) <> (DescriptionP.tupled, DescriptionP.unapply)
}

object DescriptionPTable {
  val descriptionP = TableQuery[CategoryPTable]
}
