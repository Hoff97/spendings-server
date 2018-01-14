package controllers

import scala.concurrent.ExecutionContext

import com.mohiva.play.silhouette.api._
import javax.inject._
import play.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json._
import play.api.mvc._
import spendings.model._
import spendings.auth._
import spendings.service._
import akka.util._
import java.io._
import org.apache.commons.io._
import scala.util._

class ImageController @Inject()(cc: ControllerComponents,
                                silhouette: Silhouette[AuthEnv],
                                imageService: ImageService)
    (implicit context: ExecutionContext) extends AbstractController(cc) {

  val log = Logger("api.image")

  def scanImage = Action(parse.multipartFormData) { request =>
    log.debug("Rest request to scan image")

    request.body.file("image").map { x =>
      val file = x.ref.path.toFile()
      val str = new FileInputStream(file)
      val bytes = IOUtils.toByteArray(str)

      imageService.scan(bytes) match {
        case Success(e) => {
          log.debug(e)
          Ok(e)
        }
        case Failure(e) => BadRequest
      }
    }.getOrElse(NotImplemented)
  }
}
