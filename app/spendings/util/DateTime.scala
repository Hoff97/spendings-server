package spendings.util

import java.sql.Timestamp
import java.text.SimpleDateFormat
import play.api.mvc._
import play.api.libs.json._

object DateTime {
  implicit object timestampFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd")
    def reads(json: JsValue) = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }
    def writes(ts: Timestamp) = JsString(format.format(ts))
  }


  implicit def queryStringBinder(implicit stringBinder: QueryStringBindable[String]) = new QueryStringBindable[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd")

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Timestamp]] = {
      for {
        str <- stringBinder.bind(key, params)
      } yield {
        str match {
          case Right(r) => Right(new Timestamp(format.parse(r).getTime))
          case _ => Left("Couldnt match timestamp")
        }
      }
    }
    override def unbind(key: String, ts: Timestamp): String = key + "=" + JsString(format.format(ts))

  }
}
