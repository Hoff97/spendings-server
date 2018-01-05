package controllers

import scala.concurrent.ExecutionContext

import com.mohiva.play.silhouette.api._
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
import spendings.auth._

class SpendingController @Inject()(cc: ControllerComponents,
                                   protected val dbConfigProvider: DatabaseConfigProvider,
                                   silhouette: Silhouette[AuthEnv])
    (implicit context: ExecutionContext)
    extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile]{

  val log = Logger("api.spendings")

  def getSpending(id: Int) = silhouette.SecuredAction.async { implicit request =>
    log.debug("Rest request to get spending")

    val q = for(s <- spending if s.id === id.bind
                if s.userFk === request.identity.id.getOrElse(-1)) yield s;

    db.run(q.detailed).map(x => x.headOption match {
      case Some(r) => Ok(Json.toJson(r))
      case _ => NotFound
    })
  }

  def deleteSpending(id: Int) = silhouette.SecuredAction.async { implicit request =>
    log.debug("Rest request to delete spending")

    val q = spending.filter(x => x.id === id.bind && x.userFk === request.identity.id.getOrElse(-1)).delete

    db.run(q).map {
      case 0 => NotFound
      case x => Ok(Json.toJson(x))
    }
  }

  def updateSpending(id: Int) = silhouette.SecuredAction.async(parse.json(spendingReads)) { implicit request =>
    log.debug("Rest request to update spending")

    val spend = request.body.copy(userFk = request.identity.id.getOrElse(-1))
    val q = spending.filter(x => x.id === id.bind && x.userFk === request.identity.id.getOrElse(-1)).update(spend)

    db.run(q).map {
      case 0 => NotFound
      case x => Ok(Json.toJson(x))
    }
  }

  def searchSpendings(search: Option[String], category: Option[Int],
                      sort:Option[String], sortDir: Boolean,
                      from: Option[java.sql.Timestamp], to: Option[java.sql.Timestamp]) = silhouette.SecuredAction.async { implicit request =>
    log.debug("Rest request to search Spendings")

    val fromO = from.getOrElse(new Timestamp(0,0,0,0,0,0,0))
    val toO = to.getOrElse(new Timestamp(40000,0,0,0,0,0,0))
    toO.setHours(23)
    toO.setMinutes(59)
    toO.setSeconds(59)

    val q = for {
      s <- spending if s.description like search.map(y => "%"++y++"%").getOrElse("%%").bind
      if s.categoryFk === category.getOrElse(-1) || category.getOrElse(-1).bind === -1
      if s.date >= fromO
      if s.date <= toO
      if s.userFk === request.identity.id.getOrElse(-1)
    } yield s

    val s = q.sortColumn(sort,sortDir).queryPaged.detailed
    returnPaged(s,q,db)
  }

  def sumSpendings(from: java.sql.Timestamp, to: java.sql.Timestamp) = silhouette.SecuredAction.async { implicit request =>
    log.debug("Rest request to sum spendings")

    to.setHours(23)
    to.setMinutes(59)
    to.setSeconds(59)

    val q = for {
      s <- spending if s.date >= from
      if s.date <= to
      if s.userFk === request.identity.id.getOrElse(-1)
      c <- CategoryTable.category if c.id === s.categoryFk
    } yield (s,c)

    val s = q.groupBy(_._1.categoryFk).map{ case (c,sc) => (sc.map(_._2.name).min, sc.map(_._1.amount).sum, sc.map(_._1.amount).avg, sc.length) }
    db.run(s.result).map(ls => Ok(Json.toJson(ls.map {
      case (n,s,a,c) => Sum(n.getOrElse(""),s.map(_.toDouble).getOrElse(0.0),a.map(_.toDouble).getOrElse(0.0),c)
    })))
  }

  def createSpending() = silhouette.SecuredAction.async(parse.json(spendingReads)) { implicit request =>
    log.debug("Rest request to create spending")

    val s = request.body.copy(userFk = request.identity.id.getOrElse(request.body.userFk))

    val inserted = db.run(insertAndReturn[Spending,SpendingTable](spending,s))

    inserted.map(x => Ok(Json.toJson(x)))
  }
}
