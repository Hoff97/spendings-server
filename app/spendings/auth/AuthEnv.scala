package spendings.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import spendings.model._

trait AuthEnv extends Env {
  type I = User
  type A = JWTAuthenticator
}
