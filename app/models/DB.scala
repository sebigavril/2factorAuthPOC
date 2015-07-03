package models

import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.meta.MTable

object DB {
  val db = {
    val temp = Database.forConfig("db")
    temp.withSession {
      implicit session =>
        if (MTable.getTables(Users.name).list.isEmpty)
          Users.all.ddl.create
    }
    temp
  }
}
