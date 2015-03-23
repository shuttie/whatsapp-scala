package com.github.shuttie.swhatsapp.examples

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

import akka.actor.{Props, ActorSystem}
import akka.util.Timeout
import com.github.shuttie.swhatsapp.WhatsApp
import com.github.shuttie.swhatsapp.messages._

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask

/**
 * Created by shutty on 3/23/15.
 */
object GetPicture {
  def main(args: Array[String]) {
    val system = ActorSystem.create("login")
    val wa = system.actorOf(Props(classOf[WhatsApp]), name = "whatsapp")
    implicit val timeout = Timeout(12.seconds)
    val result = Await.result(wa.ask(LoginRequest("79103408586", "X1E5cALKPQbcZ5TDmACqF5KsV98=")).mapTo[LoginResponse], 10.seconds)
    println(result)
    Thread.sleep(1000)
    //val avatar = Await.result(wa.ask(GetAvatar("79192371223")).mapTo[Avatar], 10.seconds)
    val avatar = Await.result(wa.ask(GetAvatar("79518536474")).mapTo[Avatar], 10.seconds)
    val img = avatar.picture.map(pic => ImageIO.read(new ByteArrayInputStream(pic)))
    println(avatar)
    system.awaitTermination()

  }
}
