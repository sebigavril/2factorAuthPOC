package controllers

import play.api.Play.current
import play.api.libs.ws.WS

object Dynamark {

  private val DynamarkUser = "devteampure360"
  private val DynamarkPassword = "rUc8AQu8"

  def sendSms(phoneNumber: String, message: String) = {
    WS.url("http://services.dynmark.com/HttpServices/SendMessage.ashx")
      .post(Map(
        "user" -> Seq(DynamarkUser),
        "password" -> Seq(DynamarkPassword),
        "to" -> Seq(phoneNumber),
        "text" -> Seq(message)))
  }
}
