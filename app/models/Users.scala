package models

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.TableQuery

case class User(email: String, password: String, phone: Option[String], key: String)

object Users {
  val name = "users"

  def all = TableQuery[Users]
  def allByEmail(email: String) = all.filter(_.email === email)
}

class Users(tag: Tag) extends Table[User](tag, Users.name) {
  def email = column[String]("email", O.PrimaryKey)
  def password = column[String]("password")
  def phone = column[Option[String]]("phone")
  def key = column[String]("key")

  def * = (email, password, phone, key) <> (User.tupled, User.unapply)
}