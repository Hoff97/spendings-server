package spendings.model

trait HasID[A] extends slick.lifted.AbstractTable[A] {
  def id: slick.lifted.Rep[Int]
  
  type TableElementType <: HasCopy[A]
}

trait HasCopy[A] {
  def cpy(i: Option[Int]): A
}
