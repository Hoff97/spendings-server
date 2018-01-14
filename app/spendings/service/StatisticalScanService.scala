package spendings.service

import spendings.model._
import javax.inject._
import java.sql.Timestamp
import java.text.SimpleDateFormat
import scala.concurrent._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import spendings.db.propdist._

class StatisticalScanService @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider)(implicit context: ExecutionContext)
    extends ScanService with HasDatabaseConfigProvider[JdbcProfile] {

  def scanText(text: String) = Future.never

  def improveScan(scan: Scan, spending: Spending) = Future.never
}
