package spendings.service

import spendings.model._
import scala.concurrent.Future
import org.opencv.core._
import java.awt.image._
import java.nio._
import java.io._
import scala.util._

trait ImageService {
  def scan(b: Array[Byte]): String = {
    val mat = fromByteArray(b)
    val deskewed = deskew(mat)
    val s = toInputStream(toByteArray(deskewed))
    detectText(s)
  }

  def deskew(mat: Mat): Mat

  def fromByteArray(b: Array[Byte]): Mat

  def toByteArray(mat: Mat): Array[Byte]

  def toInputStream(b: Array[Byte]): InputStream

  def detectText(s: InputStream): String
}
