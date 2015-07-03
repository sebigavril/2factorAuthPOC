package controllers

import models.UserService
import org.jboss.aerogear.security.otp.Totp
import org.jboss.aerogear.security.otp.api.Clock
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import views._

object Authentication extends Controller {

  val userService = new UserService

  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text) verifying ("Invalid email or password", result => result match {
        case (email, password) => userService.authenticate(email, password).isDefined
      }))

  val qrCodeForm = Form("verificationCode" -> text)

  def login = Action { implicit request =>
    Ok(html.login.login(loginForm))
  }

  def onLogin = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login.login(formWithErrors)),
      user => Redirect(routes.Authentication.qrcode()).withSession("email" -> user._1))
  }

  def qrcode = Action { implicit request =>
    val email = request.session.get("email").get
    userService
      .findByEmail(email)
      .map { user =>
        user.phone match {
          case Some(phone) => Dynamark.sendSms(phone, new Totp(user.key, new Clock(60)).now())
          case _ =>
        }
      }
    Ok(html.login.qrcode()).withSession("email" -> email)
  }

  def onQrcode = Action { implicit request =>
    qrCodeForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest("oups..."),
      verificationCode => {
        val email = request.session.get("email").get
        userService
          .findByEmail(email)
          .map { user =>
            val isAuthorized = user.phone match {
              case Some(phone) => new Totp(user.key, new Clock(60)).verify(verificationCode)
              case _ => new Totp(user.key).verify(verificationCode)
            }

            if (isAuthorized) Redirect(routes.Restricted.index()).withSession("email" -> email)
            else Forbidden("The code is not correct")
          }
          .getOrElse(Forbidden)
      })
  }

  def logout = Action {
    Redirect(routes.Application.index).withNewSession.flashing(
      "success" -> "You've been logged out")
  }
}