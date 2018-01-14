package spendings.service

import spendings.model._
import scala.concurrent.Future
import org.opencv.core._
import java.awt.image._
import java.nio._
import scala.util._

trait ImageService {
  def scan(b: Array[Byte]): Try[String] = {
    val mat = fromByteArray(b)
    val deskewed = deskew(mat)
    val bufImg = toBufferedImage(toByteArray(deskewed))
    detectText(bufImg)
  }

  def deskew(mat: Mat): Mat

  def fromByteArray(b: Array[Byte]): Mat

  def toByteArray(mat: Mat): Array[Byte]

  def toBufferedImage(b: Array[Byte]): BufferedImage

  def detectText(b: BufferedImage): Try[String]
}
