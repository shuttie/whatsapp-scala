package com.github.shuttie.swhatsapp.examples

import akka.actor.{Props, ActorSystem}
import akka.util.Timeout
import com.github.shuttie.swhatsapp.WhatsApp
import com.github.shuttie.swhatsapp.messages.{SendMessageRequest, LoginResponse, LoginRequest}

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask

/**
 * Created by shutty on 3/23/15.
 */
object MessageSender {
  def main(args: Array[String]) {
    val system = ActorSystem.create("login")
    val wa = system.actorOf(Props(classOf[WhatsApp]), name = "whatsapp")
    implicit val timeout = Timeout(12.seconds)
    val result = Await.result(wa.ask(LoginRequest("79103408586", "X1E5cALKPQbcZ5TDmACqF5KsV98=")).mapTo[LoginResponse], 10.seconds)
    println(result)
    Thread.sleep(1000)
    val send = Await.result(wa.ask(SendMessageRequest("79518558900", "privet")), 10.seconds)
    println(send)
    system.awaitTermination()

  }
}
