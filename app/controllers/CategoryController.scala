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
import com.mohiva.play.silhouette.api._
import spendings.auth._

class CategoryController @Inject()(cc: ControllerComponents,
                                   protected val dbConfigProvider: DatabaseConfigProvider,
                                   silhouette: Silhouette[AuthEnv])
    (implicit context: ExecutionContext)
    extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile]{

  val log = Logger("api.spendings")

  def getCategories() = silhouette.SecuredAction.async { implicit request: Request[AnyContent] =>
    log.debug("Rest request to get Categories")

    db.run(category.result).map(x => Ok(Json.toJson(x)))
  }

  def createCategory() = silhouette.SecuredAction.async(parse.json(categoryReads)) { implicit request: Request[Category] =>
    log.debug("Rest request to create category")

    val inserted = db.run(insertAndReturn[Category,CategoryTable](category,request.body))

    inserted.map(x => Ok(Json.toJson(x)))
  }
}
