package spendings.service

import spendings.model._
import javax.inject._
import java.sql.Timestamp
import java.text.SimpleDateFormat
import scala.concurrent._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import spendings.db.propdist._
import spendings.db._
import spendings.db.Util._

class StatisticalScanService @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider)(implicit context: ExecutionContext)
    extends ScanService with HasDatabaseConfigProvider[JdbcProfile] {

  val token = raw"[A-Za-z]+".r
  val tokenE = token.unanchored

  def getTokens(text: String): List[String] = text.split("\n").flatMap(x => tokenE.findAllIn(x).toList).toList

  def getCategoryProbability(c: Category, tokens: List[Int]): Future[Float] = Future.sequence(tokens.map(getCategoryProbability(c,_)))
    .zip(getCategoryProbability(c))
    .map { case (l,f) => l.product*f}

  def getCategoryProbability(c: Category, token: Int): Future[Float] = {
    val q = for (p <- CategoryPTable.categoryP if p.categoryFk === c.id && p.tokenFk === token) yield (p.count,p.total)

    db.run(q.result).map(x => x.headOption.getOrElse((1,1))).map { case (a,b) => a/b }
  }

  //TODO Make more efficient by adding count to category table
  def getCategoryProbability(c: Category): Future[Float] = db.run(SpendingTable.spending.filter(_.categoryFk === c.id).length.result)
    .zip(db.run(SpendingTable.spending.length.result)).map { case (a,b) => a/b }

  def getCategories(): Future[List[Category]] = db.run(CategoryTable.category.result).map(_.toList)

  def getTokenIds(tokens: List[String]): Future[List[Int]] = Future.sequence(tokens.map(getTokenId(_)))

  def getTokenId(token: String): Future[Int] = {
    val q = TokenTable.token.filter(x => x.name === token).map(_.id)

    db.run(q.result).map(_.headOption).flatMap {
      case Some(x) => Future.successful(x)
      case None => db.run(insertAndReturn[Token,TokenTable](TokenTable.token,Token(None,token))).map(_.id.getOrElse(0))
    }
  }


  def scanText(text: String) = {
    val tokens = getTokens(text).filter(_.length>2).map(_.toLowerCase)
    val tokenIds = getTokenIds(tokens)

    val cats = tokenIds.zip(getCategories()).flatMap { case (ids,categories) =>
      println(ids)
      println(categories)

      Future.sequence(categories.map(getCategoryProbability(_,ids))).map(_.zip(categories))
    }.map { case l =>
        l.sortBy(_._1).map(_._2)
    }

    val prices = Future.successful(List())

    val dates = Future.successful(List())

    val descr = Future.successful(List())

    cats.zip(prices).zip(dates).zip(descr).map { case (((category,price),date),description) =>
      ScanResult(price,category,description,date)
    }
  }
  //TODO: Erkennen aller tokens im Text (regex, nur Buchstaben sonst nichts)
  //Abfragen der einzelnen Wahrscheinlichkeiten P(cat|t1..tn)=P(cat|t1)*...*P(cat|tn)
  //Sortieren der Kategorien/Beschreibungen nach Wahrscheinlichkeiten
  //Für Beschreibungen sind alle Token Kandidaten, daraus die 3 Wahrscheinlichsten wählen
  //Für Amount, Date sind alle Zeilen, in der die Regex (siehe SimpleService) passt Kandidaten, die Token
  //werden nur aus der Zeile genommen, daraus Berechnet sich P(zi|t1..t(ni))

  def improveScan(scan: Scan, spending: Spending) = Future.never
  //TODO: Wahrscheinlichkeiten für category, description, amount, date(vlt nur wenn gesetztes Element der gefundenen?) anpassen
}
