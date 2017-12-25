package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import whatson.db.EventTable
import javax.inject._
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import whatson.model.Event._
import whatson.model.Event
import whatson.db.EventTable._
import whatson.db.Util._
import java.sql.Timestamp
import java.time.LocalDateTime
import play.api._
import slick.jdbc.DB2Profile

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class HomeControllerSpec extends PlaySpec 
  with GuiceOneAppPerTest with Injecting {

  val log = Logger("api.events")
  
  "HomeController GET" should {
    
    "render the index page from a new instance of controller" in {
      val events = route(app, FakeRequest(GET, "/api/categories")).get
      
      status(events) mustBe OK
    }
  }
}
