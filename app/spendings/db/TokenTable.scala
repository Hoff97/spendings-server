package spendings.db

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape.proveShapeOf
import spendings.model._

class TokenTable(tag: Tag) extends Table[Token](tag, "token") with HasID[Token] {
  def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
  def name = column[String]("text",O.Unique)

  def * = (id.?,name) <> (Token.tupled, Token.unapply)
}

object TokenTable {
  val token = TableQuery[TokenTable]
}
