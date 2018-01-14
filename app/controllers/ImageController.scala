package controllers

import scala.concurrent.ExecutionContext

import com.mohiva.play.silhouette.api._
import javax.inject._
import play.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json._
import play.api.mvc._
import spendings.model.ScanResult._
import spendings.auth._
import spendings.service._
import akka.util._
import java.io._
import org.apache.commons.io._
import scala.util._
import scala.concurrent.Future

class ImageController @Inject()(cc: ControllerComponents,
                                silhouette: Silhouette[AuthEnv],
                                imageService: ImageService,
                                scanService: ScanService)
    (implicit context: ExecutionContext) extends AbstractController(cc) {

  val log = Logger("api.image")

  def scanImage = Action(parse.multipartFormData) { request =>
    log.debug("Rest request to scan image")

    request.body.file("image").map { x =>
      val file = x.ref.path.toFile()
      val str = new FileInputStream(file)
      val bytes = IOUtils.toByteArray(str)

      val res = imageService.scan(bytes)
      Ok(res)
    }.getOrElse(BadRequest)
  }

  def scanSpending = Action(parse.multipartFormData).async { request =>
    log.debug("Rest request to scan image for spending data")

    request.body.file("image").map { x =>
      val file = x.ref.path.toFile()
      val str = new FileInputStream(file)
      val bytes = IOUtils.toByteArray(str)

      val res = imageService.scan(bytes)

      scanService.scanText(res).map(x => Ok(Json.toJson(x)))
    }.getOrElse(Future.successful(BadRequest))
  }
}
