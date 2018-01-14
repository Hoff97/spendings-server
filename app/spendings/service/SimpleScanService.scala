package spendings.service

import spendings.model._
import javax.inject._
import java.sql.Timestamp
import java.text.SimpleDateFormat
import scala.concurrent.Future


class SimpleScanService @Inject()()
    extends ScanService {

  val price = raw"(\d{1,10})[.,](\d{2})".r
  val priceE = price.unanchored

  val priceIndicator = raw"(?i)(sum|summe|gesamt)".r
  val priceIndicatorE = priceIndicator.unanchored

  val date = raw"(\d{2,4})([.-])(\d{1,2})[.-](\d{2,4})".r
  val dateE = date.unanchored

  val dateIndicator = raw"(date|datum|dat)/i".r
  val dateIndicatorE = priceIndicator.unanchored

  val format = new SimpleDateFormat("yyyy-MM-dd")

  def scanText(text: String) = {
    val lines = text.split("\n")

    val prices = lines.map { x =>
      x match {
        case priceE(b,a) => Some(((b,a),x))
        case _ => None
      }
    }.flatten.sortBy { case (_,t) =>
        priceIndicatorE.findAllIn(t).length
    }.map {  case ((a,b),_) => new BigDecimal(new java.math.BigDecimal(a+"."+b)) }


    val dates = lines.map { x =>
      x match {
        case dateE(a,s,b,c) => Some(((a,s,b,c),x))
        case _ => None
      }
    }.flatten.sortBy { case (_,t) =>
      dateIndicatorE.findAllIn(t).length
    }.map { case ((a,s,b,c),_) =>
        if(s=="-" && a.length == 4){
          new Timestamp(format.parse(a+"-"+b+"-"+c).getTime)
        } else {
          new Timestamp(format.parse(c+"-"+b+"-"+a).getTime)
        }
    }

    Future.successful(ScanResult(prices.toList,List(),List(),dates.toList))
  }

  def improveScan(scan: Scan, spending: Spending) = Future.successful()
}
