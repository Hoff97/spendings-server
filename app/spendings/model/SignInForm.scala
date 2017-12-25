package spendings.model

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class SignInForm(email: String, password: String, rememberMe: Boolean)

object SignInForm {
  /**
    * Converts the JSON into a `SignInForm.Data` object.
    */
  implicit val dataReads = (
    (__ \ 'email).read[String] and
      (__ \ 'password).read[String] and
      (__ \ 'rememberMe).read[Boolean]
  )(SignInForm.apply _)
}
