package spendings.db

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape.proveShapeOf
import spendings.model._

class CategoryTable(tag: Tag) extends Table[Category](tag, "category") with HasID[Category] {
  def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
  def name = column[String]("name",O.Unique)

  def * = (id.?,name) <> (Category.tupled, Category.unapply)
}

object CategoryTable {
  val category = TableQuery[CategoryTable]
}
