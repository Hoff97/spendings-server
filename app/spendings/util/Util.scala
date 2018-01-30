package spendings.util

import scala.concurrent._

object Util {
  implicit class ListUtils[A](list: List[A])(implicit executionContext: ExecutionContext) {
    def mapFuture[B](f: A => Future[B]): Future[List[B]] = Future.sequence(list.map(f))
  }

  implicit class FutureUtils[A](f: Future[A])(implicit executionContext: ExecutionContext) {
    def thenZip[B](g: Future[B]): Future[(A,B)] = f.flatMap(x => g.map(y => (x,y)))
  }
}
