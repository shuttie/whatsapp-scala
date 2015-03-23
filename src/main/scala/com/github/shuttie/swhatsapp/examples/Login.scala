package com.github.shuttie.swhatsapp.examples

import akka.actor.{Props, ActorSystem}
import akka.util.Timeout
import com.github.shuttie.swhatsapp.WhatsApp
import com.github.shuttie.swhatsapp.messages.{LoginResponse, LoginRequest}

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
/**
 * Created by shutty on 3/20/15.
 */
object Login {
  def main(args: Array[String]) {
    val system = ActorSystem.create("login")
    val wa = system.actorOf(Props(classOf[WhatsApp]), name = "whatsapp")
    implicit val timeout = Timeout(12.seconds)
    val result = Await.result(wa.ask(LoginRequest("79103408586", "X1E5cALKPQbcZ5TDmACqF5KsV98=")).mapTo[LoginResponse], 10.seconds)
    println(result)
    system.awaitTermination()
  }
}
