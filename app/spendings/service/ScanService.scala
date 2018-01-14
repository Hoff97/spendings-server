package spendings.service

import spendings.model._
import scala.concurrent.Future
import org.opencv.core._
import java.awt.image._
import java.nio._
import java.io._
import scala.util._

trait ScanService {
  def scanText(text: String): Future[ScanResult]

  def improveScan(scan: Scan, spending: Spending): Future[Unit]
}
