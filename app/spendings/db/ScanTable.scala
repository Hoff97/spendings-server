package spendings.db

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape.proveShapeOf
import spendings.model._

class ScanTable(tag: Tag) extends Table[Scan](tag, "scan") with HasID[Scan] {
  def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
  def result = column[String]("result")

  def * = (id.?,result) <> (Scan.tupled, Scan.unapply)
}

object ScanTable {
  val scan = TableQuery[ScanTable]
}
