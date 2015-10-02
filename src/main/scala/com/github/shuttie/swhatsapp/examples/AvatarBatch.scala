package com.github.shuttie.swhatsapp.examples

import java.io.{File, FileOutputStream}

import akka.actor.{ActorLogging, Props, ActorSystem, Actor}
import com.github.shuttie.swhatsapp.WhatsApp
import com.github.shuttie.swhatsapp.messages.{Avatar, GetAvatar, LoginSuccessful, LoginRequest}
import scala.collection.mutable
import scala.io.Source

/**
 * Created by shutty on 3/23/15.
 */

class Processor extends Actor with ActorLogging {
  val queue = mutable.Queue[String](Source.fromFile("/home/shutty/phones.csv").getLines().toSeq: _*)
  var done = 0
  var found = 0
  val whatsapp = context.actorOf(Props(classOf[WhatsApp]), name = "whatsapp")
  whatsapp ! LoginRequest("79103408586", "X1E5cALKPQbcZ5TDmACqF5KsV98=")
  def receive = {
    case LoginSuccessful => next
    case Avatar(from, pic) if pic.isDefined => {
      log.info("MATCH")
      found += 1
      done += 1
      val stream = new FileOutputStream(new File(s"/tmp/$from.jpg"))
      stream.write(pic.get)
      stream.close()
      status
      next
    }
    case Avatar(from, pic) if !pic.isDefined => {
      log.info("MISS")
      done += 1
      status
      next
    }
  }
  def next = {
    if (queue.nonEmpty) {
      val next = queue.dequeue()
      //Thread.sleep(1000)
      whatsapp ! GetAvatar(next)
    }
  }
  def status = {
    log.info(s"total: $done, found: $found, rate = ${100.0 * found.toFloat / done.toFloat}%")
  }
}
object AvatarBatch {
  def main (args: Array[String]) {
    val system = ActorSystem.create("login")
    system.actorOf(Props(classOf[Processor]), "proc")
    system.awaitTermination()
  }

}
