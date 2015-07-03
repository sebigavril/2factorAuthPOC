package controllers

import models.{User, UserService}
import org.jboss.aerogear.security.otp.Totp
import org.jboss.aerogear.security.otp.api.{Base32, Clock}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}
import views.html

object Registration extends Controller {

  val userService = new UserService

  val registerForm = Form(
    tuple(
      "email" -> text,
      "password" -> text) verifying ("User already exists", result => result match {
        case (email, _) => !userService.findByEmail(email).isDefined
      }))

  val verificationForm = Form("verificationCode" -> text)

  val phoneNumberForm = Form("phoneNumber" -> text)

  def register = Action { implicit request =>
    Ok(html.register.register(registerForm))
  }

  def onRegister = Action { implicit request =>
    registerForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.register.register(formWithErrors)),
      user => {
        val secret = Base32.random()
        val totp = new Totp(secret)
        userService.create(User(user._1, user._2, None, secret))

        val otpAuthURL = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=" + totp.uri(s"Pure360:${user._1}") + "&issuer=" + "Pure360"

        Redirect(routes.Registration.qrcode())
          .withSession(
            "email" -> user._1,
            "otpAuthURL" -> otpAuthURL)
      })
  }

  def qrcode = Action {
    request =>
      val email = request.session.get("email").get
      val otpAuthURL = request.session.get("otpAuthURL").get
      userService
        .findByEmail(email)
        .map { user => Ok(views.html.register.qrcode(otpAuthURL, user.key)) }
        .getOrElse(Forbidden)
  }

  def onQrcode = Action { implicit request =>
    verificationForm.bindFromRequest.fold(
      formWithErrors => BadRequest("oups..."),
      verificationCode => {
        val email = request.session.get("email").get
        userService
          .findByEmail(email)
          .map { user =>
            val secret = user.key
            val isAuthorized = new Totp(secret).verify(verificationCode)

            if (isAuthorized) Ok(html.restricted(user))
            else Forbidden("The code is not correct")
          }
          .getOrElse(Forbidden)
      })
  }

  def sms = Action { implicit request =>
    Ok(html.register.sms(request.session.get("email").get))
  }

  def onSms = Action { implicit request =>
    phoneNumberForm.bindFromRequest.fold(
      formWithErrors => BadRequest("oups..."),
      phoneNumber => {
        val email = request.session.get("email").get
        userService
          .findByEmail(email)
          .map { user =>
            userService.addPhone(email, phoneNumber)
            val secret = user.key
            val verificationCode = new Totp(secret, new Clock(60)).now()
            Dynamark.sendSms(phoneNumber, verificationCode)
            Ok(html.register.smsverification(user.key))
          }
          .getOrElse(Forbidden)
      })
  }

  def smsVerification = Action {
    request =>
      val email = request.session.get("email").get
      userService
        .findByEmail(email)
        .map { user => Ok(views.html.register.smsverification(user.key)) }
        .getOrElse(Forbidden)
  }

  def onSmsVerification = Action { implicit request =>
    verificationForm.bindFromRequest.fold(
      formWithErrors => BadRequest("oups..."),
      verificationCode => {
        val email = request.session.get("email").get
        userService
          .findByEmail(email)
          .map { user =>
          val secret = user.key
          val isAuthorized = new Totp(secret, new Clock(60)).verify(verificationCode)

          if (isAuthorized) Ok(html.restricted(user))
          else Forbidden("The code is not correct")
        }
          .getOrElse(Forbidden)
      })
  }
}
