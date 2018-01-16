package spendings.service

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import scala.concurrent.Future
import spendings.model._
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import javax.inject._
import slick.jdbc.JdbcProfile
import spendings.db.UserTable._
import slick.jdbc.PostgresProfile.api._
import com.mohiva.play.silhouette.api.util.PasswordInfo
import scala.concurrent.ExecutionContext
import spendings.db._
import spendings.db.Util._
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgcodecs.Imgcodecs._
import org.opencv.core._
import org.opencv.imgproc.Imgproc
import java.nio._
import java.io._
import javax.imageio._
import java.awt.image._
import scala.util._
import scala.sys.process._


class ImageServiceImpl @Inject()(implicit context: ExecutionContext)
    extends ImageService {
  nu.pattern.OpenCV.loadLocally()


  def deskew(mat: Mat): Mat = {
    val t = mat.clone()
    Imgproc.adaptiveThreshold(t, t, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 127, 10);
    val copy = t.clone()

    Core.bitwise_not(t,t)

    val lines = new Mat()
    Imgproc.HoughLinesP(t,lines,1,scala.math.Pi/180,100, t.cols()/2, 40)
    var angle = 0.0
    for(i <- 0 to (lines.rows()-1)) {
      val row = lines.get(i,0)
      val a = scala.math.atan2(row(3) - row(1), row(2) - row(0))
      angle += a
    }
    angle /= lines.rows()
    angle = angle/scala.math.Pi*180
    println(angle)
    if(angle< -45.0)
      angle += 90
    else if(angle > 45)
      angle -= 90

    val rotated = new Mat()
    val rotate = Imgproc.getRotationMatrix2D(new Point(copy.rows()/2,copy.cols()/2),angle,1)
    Imgproc.warpAffine(copy,rotated,rotate,copy.size())
    Imgcodecs.imwrite("yay.png", rotated)
    rotated

  }

  def fromByteArray(b: Array[Byte]) = Imgcodecs.imdecode(new MatOfByte(b:_*), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

  def toByteArray(mat: Mat): Array[Byte] = {
    val bufMat = new MatOfByte()
    imencode(".png", mat, bufMat)
    bufMat.toArray()
  }

  def toInputStream(b: Array[Byte]) = new ByteArrayInputStream(b)

  def detectText(s: InputStream) = ("tesseract stdin stdout -l deu" #< s).!!
}
