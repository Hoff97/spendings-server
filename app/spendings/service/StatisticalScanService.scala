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
import spendings.util.Util._
import spendings.model.propdist._

class StatisticalScanService @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider)(implicit context: ExecutionContext)
    extends ScanService with HasDatabaseConfigProvider[JdbcProfile] {

  val token = raw"[A-Za-z]+".r
  val tokenE = token.unanchored

  val price = raw"(\d{1,10})\s?[\s.,]\s?(\d{2})".r
  val priceE = price.unanchored

  val format = new SimpleDateFormat("yyyy-MM-dd")

  //TODO: Check wich of these works better
  def upperWilson(c: Int, t: Int): Double = {
    val zalpha = 1.96
    val zalpha2 = Math.pow(zalpha,2)
    val cf = c.toFloat/t
    (cf+(zalpha2/(2*t))+zalpha*Math.sqrt((cf*(1-cf)+zalpha2/(4*t))/t))/(1+zalpha2/t)
  }

  def lowerWilson(c: Int, t: Int): Double = {
    val zalpha = 1.96
    val zalpha2 = Math.pow(zalpha,2)
    val cf = c.toFloat/t
      (cf+(zalpha2/(2*t))-zalpha*Math.sqrt((cf*(1-cf)+zalpha2/(4*t))/t))/(1+zalpha2/t)
  }

  def findPrices(str: String): List[BigDecimal] = str match {
    case priceE(a,c) => List(new BigDecimal(new java.math.BigDecimal(a+"."+c)))
    case _ => List()
  }

  def findDates(str: String): List[Timestamp] = str match {
    case dateE(a,s,b,c) =>
      if(s=="-" && a.length == 4)
        List(new Timestamp(format.parse(a+"-"+b+"-"+c).getTime))
      else
        List(new Timestamp(format.parse(c+"-"+b+"-"+a).getTime))
    case _ => List()
  }

  def prodDef(l: List[Double], default: Double) = if(l.length == 0) default else l.product
  def prop(l: List[Double], default: (Int,Int), p: Double) = prodDef(l,upperWilson(default._1,default._2))*p

  val date = raw"(\d{2,4})\s?([\s.-])\s?(\d{1,2})\s?[\s.-]\s?(\d{2,4})".r
  val dateE = date.unanchored

  def getTokens(text: String): List[String] = getTokens(text.split("\n"))
  def getTokens(text: Array[String]): List[String] = {
    val res = text.flatMap(x => tokenE.findAllIn(x).toList)
      .toList.filter(_.length>2).map(_.toLowerCase)
    res.filter(x => !res.exists(y => (y contains x) && y.length>x.length))
  }

  def getCategoryProbability(c: Category, tokens: List[Int], default: (Int,Int)): Future[Double] = Future.sequence(tokens.map(getCategoryProbability(c,_,default)))
    .zip(getCategoryProbability(c))
    .map { case (l,f) => prop(l,default,f)}

  def getCategoryProbability(c: Category, token: Int, default: (Int,Int)): Future[Double] = {
    val q = for (p <- CategoryPTable.categoryP if p.categoryFk === c.id && p.tokenFk === token) yield (p.count,p.total)

    db.run(q.result).map(x => x.headOption.getOrElse(default)).map { case (a,b) => upperWilson(a, b) }
  }

  def getCategoryProbability(c: Category): Future[Double] = db.run(SpendingTable.spending.filter(_.categoryFk === c.id).length.result)
    .zip(db.run(SpendingTable.spending.length.result)).map { case (a,b) => upperWilson(a, b) }

  def getPriceProbability(price: BigDecimal, tokens: List[Int], default: (Int,Int)): Future[Double] = Future.sequence(tokens.map(getPriceProbability(_,default)))
    .zip(getPriceProbability(price))
    .map { case (l,f) => prop(l,default,f)}
  def getPriceProbability(token: Int, default: (Int,Int)): Future[Double] = {
    val q = for (p <- AmountPTable.amountP if p.tokenFk === token) yield (p.count,p.total)

    db.run(q.result).map(x => x.headOption.getOrElse(default)).map { case (a,b) => upperWilson(a, b) }
  }
  //TODO Normal distribution using spendings tables average and stddev
  def getPriceProbability(price: BigDecimal): Future[Double] = Future.successful(1)

  def getDateProbability(date: Timestamp, tokens: List[Int], default: (Int,Int)): Future[Double] = Future.sequence(tokens.map(getDateProbability(_,default)))
    .zip(getDateProbability(date))
    .map { case (l,f) => prop(l,default,f)}
  def getDateProbability(token: Int, default: (Int,Int)): Future[Double] = {
    val q = for (p <- DatePTable.dateP if p.tokenFk === token) yield (p.count,p.total)

    db.run(q.result).map(x => x.headOption.getOrElse(default)).map { case (a,b) => upperWilson(a, b) }
  }
  //TODO Assume distribution giving more recent dates higher probability
  def getDateProbability(date: Timestamp): Future[Double] = Future.successful(1)

  def getDescriptionProbability(descr: Int, tokens: List[Int], default: (Int,Int)): Future[Double] = Future.sequence(tokens.map(getDescriptionProbability(descr,_,default)))
    .map { case l => prop(l,default,1)}
  def getDescriptionProbability(descr: Int, token: Int, default: (Int,Int)): Future[Double] = {
    val q = for (p <- DescriptionPTable.descriptionP if p.descriptionFk === descr && p.tokenFk === token) yield (p.count,p.total)

    db.run(q.result).map(x => x.headOption.getOrElse(default)).map { case (a,b) => upperWilson(a, b) }
  }

  def getCategories(): Future[List[Category]] = db.run(CategoryTable.category.result).map(_.toList)

  def getTokenIds(tokens: List[String]): Future[List[Int]] = Future.sequence(tokens.map(getTokenId(_)))

  def getAllTokenIds(): Future[List[Int]] = db.run(TokenTable.token.map(_.id).result).map(_.toList)

  def getTokenId(token: String): Future[Int] = {
    val q = TokenTable.token.filter(x => x.name === token).map(_.id)

    db.run(q.result).map(_.headOption).flatMap {
      case Some(x) => Future.successful(x)
      case None => db.run(insertAndReturn[Token,TokenTable](TokenTable.token,Token(None,token))).map(_.id.getOrElse(0))
    }
  }

  def getCategoriesDefault(): Future[(Int,Int)] = {
    db.run((CategoryPTable.categoryP.map(_.count).sum.result).
             zip(CategoryPTable.categoryP.map(_.total).sum.result)).map {
      case (x,y) => (x.getOrElse(1),y.getOrElse(1))
    }
  }

  def getPricesDefault(): Future[(Int,Int)] = {
    db.run((AmountPTable.amountP.map(_.count).sum.result).
             zip(AmountPTable.amountP.map(_.total).sum.result)).map {
      case (x,y) => (x.getOrElse(1),y.getOrElse(1))
    }
  }

  def getDatesDefault(): Future[(Int,Int)] = {
    db.run((DatePTable.dateP.map(_.count).sum.result).
             zip(DatePTable.dateP.map(_.total).sum.result)).map {
      case (x,y) => (x.getOrElse(1),y.getOrElse(1))
    }
  }

  def getDescriptionsDefault(): Future[(Int,Int)] = {
    db.run((DescriptionPTable.descriptionP.map(_.count).sum.result).
             zip(DatePTable.dateP.map(_.total).sum.result)).map {
      case (x,y) => (x.getOrElse(1),y.getOrElse(1))
    }
  }

  def getToken(id: Int): Future[String] = db.run(TokenTable.token.filter(_.id === id).map(_.name).result).map(_.headOption.getOrElse(""))

  def scanText(text: String) = {
    val lines = text.split("\n")
    val tokens = getTokens(lines)
    val tokenIds = getTokenIds(tokens)
    val allIds = getAllTokenIds()

    tokenIds.thenZip(allIds).flatMap { case (ids,allIds) =>
      val cats = getCategories().zip(getCategoriesDefault()).flatMap { case (categories,default) =>
        println(ids.length)

        Future.sequence(categories.map(getCategoryProbability(_,ids,default))).map(_.zip(categories))
      }.map { case l =>
          l.sortBy(x => -x._1).map(_._2)
      }

      val prices = lines.map(x => (x,findPrices(x))).map{ case (a,b) => (a,b.toList) }
        .filter(_._2.length>0).toList
        .mapFuture { case (l,p) =>
          getTokenIds(getTokens(l)).map(t => (p,t))
        }.zip(getPricesDefault()).flatMap { case (l,d) =>
          println(d)
          l.mapFuture { case (prices,t) => prices.mapFuture(x => getPriceProbability(x,t,d).map(p => (x,p))) }
        }.map { l =>
          println(l.flatten.sortBy(x => -x._2))
          l.flatten.sortBy(x => -x._2).map(x => x._1)
        }

      val dates = lines.map(x => (x,findDates(x))).map{ case (a,b) => (a,b.toList) }
        .filter(_._2.length>0).toList
        .mapFuture { case (l,p) =>
          getTokenIds(getTokens(l)).map(t => (p,t))
        }.zip(getPricesDefault()).flatMap { case (l,d) =>
          l.mapFuture { case (dates,t) => dates.mapFuture(x => getDateProbability(x,t,d).map(p => (x,p))) }
        }.map { l =>
          l.flatten.sortBy(x => -x._2).map(x => x._1)
        }

      val descr = getDescriptionsDefault.flatMap { case d =>
        allIds.mapFuture(x => getDescriptionProbability(x,ids,d).map(p => (x,p)))
      }.map{d =>
        d.sortBy(x => -x._2).filter(x => !x._2.isNaN()).map(_._1).take(3)
      }
        .flatMap { d =>
          d.mapFuture(getToken(_)).map(x => x.fold(""){ case (a,b) => if (a.length==0) b else a++" "++b})
        }

      val createScan = db.run(insertAndReturn[Scan,ScanTable](ScanTable.scan, Scan(None,text)))

      cats.zip(prices).zip(dates).zip(descr).zip(createScan).map { case ((((category,price),date),description),scan) =>
        ScanResult(scan.id.getOrElse(-1),price,category,description,date)
      }
    }
  }

  def updateCategoryProbabilities(c: Int, tokens: List[(Int,Int)]): Future[List[Int]] = Future.sequence(tokens.map(x => updateCategoryProbability(c,x)))

  def updateCategoryProbability(c: Int, token: (Int,Int)): Future[Int] = {
    val q = for (p <- CategoryPTable.categoryP if p.categoryFk === c && p.tokenFk === token._1) yield (p.count,p.total)

    db.run(q.result).map(x => x.headOption).flatMap {
      case Some((c,t)) => db.run(q.update((c+token._2,t+1)))
      case None => db.run(CategoryPTable.categoryP += CategoryP(None,c,token._1,token._2,1))
    }
  }

  def updateDescriptionobabilities(d: Int, tokens: List[(Int,Int)]): Future[List[Int]] = Future.sequence(tokens.map(x => updateDescriptionobabilitiy(d,x)))

  def updateDescriptionobabilitiy(d: Int, token: (Int,Int)): Future[Int] = {
    val q = for (p <- DescriptionPTable.descriptionP if p.descriptionFk === d && p.tokenFk === token._1) yield (p.count,p.total)

    db.run(q.result).map(x => x.headOption).flatMap {
      case Some((c,t)) => db.run(q.update((c+token._2,t+1)))
      case None => db.run(DescriptionPTable.descriptionP += DescriptionP(None,d,token._1,token._2,1))
    }
  }

  def updatePriceProbabilities(tokens: List[Int], u: Int): Future[List[Int]] = Future.sequence(tokens.map(x => updatePriceProbabilitiy(x,u)))

  def updatePriceProbabilitiy(token: Int, u: Int): Future[Int] = {
    val q = for (p <- AmountPTable.amountP if p.tokenFk === token) yield (p.count,p.total)

    db.run(q.result).map(x => x.headOption).flatMap {
      case Some((c,t)) => db.run(q.update((c+u,t+1)))
      case None => db.run(AmountPTable.amountP += AmountP(None,token,u,1))
     }
  }

  def updateDateProbabilities(tokens: List[Int], u: Int): Future[List[Int]] = Future.sequence(tokens.map(x => updateDateProbability(x,u)))

  def updateDateProbability(token: Int, u: Int): Future[Int] = {
    val q = for (p <- DatePTable.dateP if p.tokenFk === token) yield (p.count,p.total)

    db.run(q.result).map(x => x.headOption).flatMap {
      case Some((c,t)) => db.run(q.update((c+u,t+1)))
      case None => db.run(DatePTable.dateP += DateP(None,token,u,1))
    }
  }

  def improveScan(scan: Scan, spending: Spending) = {
    val lines = scan.result.split("\n")
    val descr = getTokens(spending.description)
    val tokens = getTokens(lines)++descr
    val tokenIds = getTokenIds(tokens)
    val descrIds = getTokenIds(descr)
    val allIds = getAllTokenIds()

    tokenIds.thenZip(descrIds).thenZip(allIds).flatMap { case ((ids,dIds),allIds) =>
      val categories = updateCategoryProbabilities(spending.categoryFk, allIds.map(x => (x,if(ids.contains(x)) 1 else 0)))
      val descriptions = Future.sequence(dIds.map(d => updateDescriptionobabilities(d, allIds.map(x => (x,if(ids.contains(x)) 1 else 0)))))

      val prices = lines.map(x => (x,findPrices(x))).map{ case (a,b) => (a,b.toList) }
        .filter(_._2.length>0).toList
        .mapFuture { case (l,p) =>
          getTokenIds(getTokens(l)).map(t => (p,t))
        }.flatMap { l =>
          l.mapFuture { case (prices,t) =>
            prices.mapFuture(x => updatePriceProbabilities(t,if(spending.amount==x) 1 else 0))
          }
        }

      val dates = lines.map(x => (x,findDates(x))).map{ case (a,b) => (a,b.toList) }
        .filter(_._2.length>0).toList
        .mapFuture { case (l,p) =>
          getTokenIds(getTokens(l)).map(t => (p,t))
        }.flatMap { l =>
          l.mapFuture { case (dates,t) =>
            dates.mapFuture(x => updateDateProbabilities(t,if(spending.date==x) 1 else 0))
          }
        }

      categories
        .zip(descriptions)
        .zip(prices)
        .zip(dates)
        .map (x => ())
    }

    //TODO Delete scan
  }
}
