package spendings.service

import scala.concurrent.ExecutionContext

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo, AuthInfo }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import scala.concurrent.Future
import scala.reflect.ClassTag
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import javax.inject._
import slick.jdbc.JdbcProfile
import spendings.db.UserTable._
import slick.jdbc.PostgresProfile.api._
import com.mohiva.play.silhouette.api.util.PasswordInfo
import play.api.{Configuration, Logger}

class AuthInfoService @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider)(implicit context: ExecutionContext)
    extends AuthInfoRepository with HasDatabaseConfigProvider[JdbcProfile] {

  val log = Logger("api.events")

  /**
   * Finds the auth info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @param tag The class tag of the auth info.
   * @tparam T The type of the auth info to handle.
   * @return The found auth info or None if no auth info could be found for the given login info.
   */
  def find[T <: AuthInfo](loginInfo: LoginInfo)(implicit tag: ClassTag[T]): Future[Option[T]] = {
    val q = for(u <- user if u.providerId === loginInfo.providerID && u.providerKey === loginInfo.providerKey) yield (u.pwHash,u.pwSalt,u.pwHasher)

    db.run(q.result).map(f => f.headOption.map(x => PasswordInfo(x._3, x._1, Some(x._2)).asInstanceOf[T]))
  }

  /**
   * Adds new auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The auth info to save.
   * @tparam T The type of the auth info to handle.
   * @return The saved auth info.
   */
  def add[T <: AuthInfo](loginInfo: LoginInfo, authInfo: T): Future[T] = {
    authInfo match {
      case PasswordInfo (a,b,c) => {
        val q = for {
          u <- user if u.providerId === loginInfo.providerID && u.providerKey === loginInfo.providerKey
        } yield (u.pwHash,u.pwSalt,u.pwHasher)

        db.run(q.update((b,c.getOrElse(""),a))).map ( x => PasswordInfo(a,b,c).asInstanceOf[T] )
      }
      case _ => Future.never
    }
  }

  /**
   * Updates the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be updated.
   * @param authInfo The auth info to update.
   * @tparam T The type of the auth info to handle.
   * @return The updated auth info.
   */
  def update[T <: AuthInfo](loginInfo: LoginInfo, authInfo: T): Future[T] = {
    authInfo match {
      case PasswordInfo (a,b,c) => {
        val q = for {
          u <- user if u.providerId === loginInfo.providerID && u.providerKey === loginInfo.providerKey
        } yield (u.pwHash,u.pwSalt,u.pwHasher)

        db.run(q.update((b,c.getOrElse(""),a)))

        Future(PasswordInfo(a,b,c).asInstanceOf[T])
      }
      case _ => Future.never
    }
  }

  /**
   * Saves the auth info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The auth info to save.
   * @tparam T The type of the auth info to handle.
   * @return The updated auth info.
   */
  def save[T <: AuthInfo](loginInfo: LoginInfo, authInfo: T): Future[T] = {
    authInfo match {
      case PasswordInfo (a,b,c) => {
        val q = for {
          u <- user if u.providerId === loginInfo.providerID && u.providerKey === loginInfo.providerKey
        } yield (u.pwHash,u.pwSalt,u.pwHasher)

        db.run(q.update((b,c.getOrElse(""),a)))

        Future(PasswordInfo(a,b,c).asInstanceOf[T])
      }
      case _ => Future.never
    }
  }

  /**
    * Removes the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be removed.
    * @param tag The class tag of the auth info.
    * @tparam T The type of the auth info to handle.
    * @return A future to wait for the process to be completed.
    */
  def remove[T <: AuthInfo](loginInfo: LoginInfo)(implicit tag: ClassTag[T]): Future[Unit] = {
    val q = for {
      u <- user if u.providerId === loginInfo.providerID && u.providerKey === loginInfo.providerKey
    } yield (u.pwHash,u.pwSalt,u.pwHasher)

    db.run(q.update(("","",""))).map(f => Unit)
  }
}
