package spendings.db

import scala.language.higherKinds

trait Sortable[R,O] {
  def sortColumn[U,C[D]](name: String, dir: Boolean): R
  def sortColumn[U,C[D]](name: String, dir: Boolean, param: O): R = sortColumn(name, dir)
  
  def sortColumn[U,C[D]](name: Option[String], dir: Boolean): R = sortColumn(name.getOrElse(defaultSort),dir)
  def sortColumn[U,C[D]](name: Option[String], dir: Boolean, param: O): R = sortColumn(name.getOrElse(defaultSort),dir,param)
  
  val defaultSort: String
}
