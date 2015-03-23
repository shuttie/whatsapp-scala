package com.github.shuttie.swhatsapp.examples

import akka.actor.{Props, ActorSystem}
import com.github.shuttie.swhatsapp.WhatsApp
import com.github.shuttie.swhatsapp.messages.LoginRequest

/**
 * Created by shutty on 3/20/15.
 */
object Login {
  def main(args: Array[String]) {
    val system = ActorSystem.create("login")
    val wa = system.actorOf(Props(classOf[WhatsApp]), name = "whatsapp")
    wa ! LoginRequest("79103408586", "X1E5cALKPQbcZ5TDmACqF5KsV98=", "user", "shutty")
    system.awaitTermination()
  }
}
