package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._

object Application extends Controller {
  
  var someState: String = "Initial state" // some state that will be modified through REPL
  
  def index = Action {
    Ok(views.html.main(s"State is: $someState"))
  }

  def repl = Action {
    Ok(views.html.repl())
  }

  def replsocket = WebSocket.using[String] { request => playrepl.REPL.getNew }
}
