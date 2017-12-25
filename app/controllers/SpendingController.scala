package controllers

import scala.concurrent.ExecutionContext

import javax.inject._
import play.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json._
import play.api.mvc._
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import spendings.db._
import spendings.db.SpendingTable._
import spendings.db.Util._
import spendings.model._
import spendings.model.Spending._
import spendings.model.Search._
import spendings.model.detail.SpendingDetail._
import java.sql.Timestamp
import spendings.util.DateTime._

class SpendingController @Inject()(cc: ControllerComponents, protected val dbConfigProvider: DatabaseConfigProvider)
    (implicit context: ExecutionContext)
    extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile]{

  val log = Logger("api.spendings")

  def getSpending(id: Int) = Action.async { implicit request: Request[AnyContent] =>
    log.debug("Rest request to get spending")

    val q = for(s <- spending if s.id === id.bind) yield s;

    db.run(q.detailed).map(x => x.headOption match {
      case Some(r) => Ok(Json.toJson(r))
      case _ => NotFound
    })
  }

  def deleteSpending(id: Int) = Action.async { implicit request: Request[AnyContent] =>
    log.debug("Rest request to delete spending")

    val q = spending.filter(_.id === id.bind).delete

    db.run(q).map {
      case 0 => NotFound
      case x => Ok(Json.toJson(x))
    }
  }

  def updateSpending(id: Int) = Action.async(parse.json(spendingReads)) { implicit request: Request[Spending] =>
    log.debug("Rest request to update spending")

    val q = spending.filter(_.id === id.bind).update(request.body)

    db.run(q).map {
      case 0 => NotFound
      case x => Ok(Json.toJson(x))
    }
  }

  def searchSpendings(search: Option[String], category: Option[Int],
                      sort:Option[String], sortDir: Boolean,
                      from: Option[java.sql.Timestamp], to: Option[java.sql.Timestamp]) = Action.async { implicit request: Request[AnyContent] =>
    log.debug("Rest request to search Spendings")

    val fromO = from.getOrElse(new Timestamp(40000,0,0,0,0,0,0))
    val toO = to.getOrElse(new Timestamp(0,0,0,0,0,0,0))
    toO.setHours(23)
    toO.setMinutes(59)
    toO.setSeconds(59)

    val q = for {
      s <- spending if s.description like search.map(y => "%"++y++"%").getOrElse("%%").bind
      if s.categoryFk === category.getOrElse(-1) || category.getOrElse(-1).bind === -1
      if s.date >= fromO
      if s.date <= toO
    } yield s

    val s = q.sortColumn(sort,sortDir).queryPaged.detailed
    returnPaged(s,q,db)
  }

  def sumSpendings(from: java.sql.Timestamp, to: java.sql.Timestamp) = Action.async { implicit request: Request[AnyContent] =>
    log.debug("Rest request to sum spendings")

    to.setHours(23)
    to.setMinutes(59)
    to.setSeconds(59)

    val q = for {
      s <- spending if s.date >= from
      if s.date <= to
    } yield s

    val s = q.groupBy(_.categoryFk).map{ case (c,s) => (c,s.map(_.amount).sum,s.length) }
    db.run(s.result).map(x => Ok(Json.toJson(x)))
  }

  def createSpending() = Action.async(parse.json(spendingReads)) { implicit request: Request[Spending] =>
    log.debug("Rest request to create spending")

    val inserted = db.run(insertAndReturn[Spending,SpendingTable](spending,request.body))

    inserted.map(x => Ok(Json.toJson(x)))
  }
}
