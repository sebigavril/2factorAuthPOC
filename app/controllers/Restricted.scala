package controllers

import models.UserService
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Controller
import views._

object Restricted extends Controller with Secured {

  val userService = new UserService

  def index = IsAuthenticated {
    username =>
      _ =>
        userService.findByEmail(username)
          .map { user => Ok(html.restricted(user)) }
          .getOrElse(Forbidden)
  }
}