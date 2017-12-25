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
import spendings.db.CategoryTable._
import spendings.db.Util._
import spendings.model._
import spendings.model.Category._
import spendings.model.detail.SpendingDetail._
import java.sql.Timestamp

class CategoryController @Inject()(cc: ControllerComponents, protected val dbConfigProvider: DatabaseConfigProvider)
    (implicit context: ExecutionContext)
    extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile]{

  val log = Logger("api.spendings")

  def getCategory(id: Int) = Action.async { implicit request: Request[AnyContent] =>
    log.debug("Rest request to get category tree")

    val q = for(s <- category if s.id === id.bind) yield s;

    db.run(q.result).map(x => x.headOption match {
      case Some(r) => Ok(Json.toJson(r))
      case _ => NotFound
    })
  }

  def deleteCategory(id: Int) = Action.async { implicit request: Request[AnyContent] =>
    log.debug("Rest request to delete category")

    val q = category.filter(_.id === id.bind).delete

    db.run(q).map {
      case 0 => NotFound
      case x => Ok(Json.toJson(x))
    }
  }

  def updateCategory(id: Int) = Action.async(parse.json(categoryReads)) { implicit request: Request[Category] =>
    log.debug("Rest request to update category")

    val q = category.filter(_.id === id.bind).update(request.body)

    db.run(q).map {
      case 0 => NotFound
      case x => Ok(Json.toJson(x))
    }
  }

  def getCategories() = Action.async { implicit request: Request[AnyContent] =>
    log.debug("Rest request to get Categories")

    db.run(category.result).map(x => Ok(Json.toJson(x)))
  }

  def createCategory() = Action.async(parse.json(categoryReads)) { implicit request: Request[Category] =>
    log.debug("Rest request to create category")

    val inserted = db.run(insertAndReturn[Category,CategoryTable](category,request.body))

    inserted.map(x => Ok(Json.toJson(x)))
  }
}
