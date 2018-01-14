package spendings.db

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape.proveShapeOf
import spendings.model._
import spendings.db.Util._
import spendings.db._
import java.sql.Timestamp

class SpendingTable(tag: Tag) extends Table[Spending](tag, "spending") with HasID[Spending] {
  def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
  def amount = column[BigDecimal]("amount")
  def description = column[String]("description")
  def date = column[Timestamp]("dayt")

  def categoryFk = column[Int]("category_fk")
  def userFk = column[Int]("user_fk")

  def scanFk = column[Int]("scan_fk")

  def creator = foreignKey("creator",userFk,UserTable.user)(_.id)
  def category = foreignKey("category",categoryFk,CategoryTable.category)(_.id)

  def * = (id.?,amount,description,date,categoryFk,userFk,scanFk.?) <> (Spending.tupled, Spending.unapply)
}

object SpendingTable {
  val spending = TableQuery[SpendingTable]

  implicit class QuerySort[C[D]](q: Query[SpendingTable,Spending,C]) extends Sortable[Query[SpendingTable,Spending,C],Unit] {
    def sortColumn[U,C[D]](name: String, dir: Boolean) = name match {
      case "date" => q.sortBy(_.date.dir(dir))
      case "amount" => q.sortBy(_.amount.dir(dir))
      case "description" => q.sortBy(_.description.dir(dir))
      case _ => q.sortBy(_.id.dir(dir))
    }

    override def sortColumn[U,C[D]](name: String, dir: Boolean, param: Unit) =
      q.sortColumn(name,dir)
    val defaultSort = "id"
  }
}
