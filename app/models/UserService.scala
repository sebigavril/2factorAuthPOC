package models

import scala.slick.driver.H2Driver.simple._

class UserService {

  def findByEmail(email: String): Option[User] = {
    DB.db.withSession { implicit s =>
      Users.allByEmail(email).list.headOption
    }
  }

  def findAll: Seq[User] = {
    DB.db.withSession { implicit s =>
      Users.all.list
    }
  }

  def authenticate(email: String, password: String): Option[User] = {
    DB.db.withSession { implicit s =>
      Users.allByEmail(email).filter(_.password === password).list.headOption
    }
  }

  def create(user: User) = {
    DB.db.withSession { implicit s =>
      Users.all += User(user.email, user.password, user.phone, user.key)
    }
  }

  def addPhone(email: String, phone: String) = {
    DB.db.withSession { implicit s =>
      Users.allByEmail(email).map(_.phone).update(Some(phone))
    }
  }
}
