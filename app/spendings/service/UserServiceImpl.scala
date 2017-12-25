package spendings.service

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import scala.concurrent.Future
import spendings.model._
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import javax.inject._
import slick.jdbc.JdbcProfile
import spendings.db.UserTable._
import slick.jdbc.PostgresProfile.api._
import com.mohiva.play.silhouette.api.util.PasswordInfo
import scala.concurrent.ExecutionContext
import spendings.db._
import spendings.db.Util._


class UserServiceImpl @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider)(implicit context: ExecutionContext)
    extends UserService with HasDatabaseConfigProvider[JdbcProfile] {
  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    db.run(user.filter(x => x.providerId === loginInfo.providerID && x.providerKey === loginInfo.providerKey).result).map(_.headOption)


  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(u: User) = db.run(insertAndReturn[User,UserTable](UserTable.user,u))

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile) = {
    val q = for {
      u <- user if u.providerId === profile.loginInfo.providerID && u.providerKey === profile.loginInfo.providerKey
    } yield u

    val u = User(None,profile.firstName + " " + profile.lastName,
                 profile.email.getOrElse(""),None,None,None,
                 profile.loginInfo.providerID,profile.loginInfo.providerKey)

    db.run(q.update(u))
    db.run(q.result).map(_.head)
  }
}
