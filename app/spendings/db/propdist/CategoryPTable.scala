package spendings.db.propdist

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape.proveShapeOf
import spendings.model.propdist._
import spendings.model._

class CategoryPTable(tag: Tag) extends Table[CategoryP](tag, "category_tokenp") with HasID[CategoryP] {
  def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
  def categoryFk = column[Int]("category_fk")
  def tokenFk = column[Int]("token_fk")

  def count = column[Int]("count")
  def total = column[Int]("total")

  def * = (id.?,categoryFk,tokenFk,count,total) <> (CategoryP.tupled, CategoryP.unapply)
}

object CategoryPTable {
  val categoryP = TableQuery[CategoryPTable]
}
