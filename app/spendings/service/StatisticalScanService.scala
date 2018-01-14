package spendings.service

import spendings.model._
import javax.inject._
import java.sql.Timestamp
import java.text.SimpleDateFormat
import scala.concurrent.Future


class StatisticalScanService @Inject()()
    extends ScanService {
  def scanText(text: String) = Future.never
}
