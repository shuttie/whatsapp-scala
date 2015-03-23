package com.github.shuttie.swhatsapp

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.net.{InetSocketAddress, URLEncoder}
import java.security.MessageDigest
import java.util.Date


import akka.actor.{Props, Status, ActorRef, LoggingFSM}
import akka.io.{IO, Tcp}
import akka.util.{Timeout, ByteString}
import com.github.shuttie.swhatsapp.messages._
import akka.pattern.ask
import com.github.shuttie.swhatsapp.protocol.Auth
import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Created by shutty on 3/20/15.
 */


class WhatsApp extends LoggingFSM[State,Context] {
  val connector = context.actorOf(Props(classOf[Connection]), name = "connection")
  val writer = new BinTreeNodeWriter
  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(LoginRequest(uid,password), Uninitialized) => {
      connector ! Connect(WhatsApp.HOST, WhatsApp.PORT)
      goto(Connecting) using LoginContext(null, sender(), uid, password)
    }
  }
  when(Connecting) {
    case Event(ConnectSuccessful, ctx:LoginContext) => {
      val resource = s"${WhatsApp.DEVICE}-${WhatsApp.VERSION}-${WhatsApp.PORT}"
      log.info(s"Resource = $resource")
      connector ! WriteBytes(writer.startStream(WhatsApp.SERVER, resource))
      connector ! WriteNode(ProtocolNode("stream:features", children = List(ProtocolNode("readreceipts"), ProtocolNode("privacy"), ProtocolNode("presence"), ProtocolNode("groups_v2"))))
      connector ! WriteNode(ProtocolNode("auth", Map("mechanism" -> "WAUTH-2", "user" -> ctx.uid)))
      goto(LoggingIn)
    }
    case Event(error, _) => {
      log.error(s"Cannot connect to server: $error")
      sender() ! LoginFailed("cannot connect to server")
      goto(Idle) using Uninitialized
    }
  }
  when(LoggingIn) {
    case Event(ProtocolNode("challenge", _, _, challengeData), ctx:LoginContext) => {
      log.info(s"received auth challenge: ${challengeData.length} bytes")
      val auth = Auth(ctx.uid, ctx.password, challengeData)
      val response = auth.response
      connector ! WriteNode(ProtocolNode("response", data = response))
      connector ! UpgradeKeys(auth)
      stay()
    }
    case Event(ProtocolNode("failure", _, _, _), ctx:LoginContext) => {
      log.error("Cannot login")
      ctx.source ! LoginFailed
      goto(Idle) using Uninitialized
    }
    case Event(ProtocolNode("success", _, _, _), ctx:LoginContext) => {
      log.info("Logged in successfully")
      ctx.source ! LoginSuccessful
      connector ! WriteNode(ProtocolNode("presence", Map("name" -> "shutty")))
      goto(LoggedIn)
    }
    case Event(node @ ProtocolNode(_, _, _, _), ctx:LoginContext) => {
      log.info(s"received no-op message: $node")
      stay()
    }
  }

  when(LoggedIn) {
    // send message
    case Event(SendMessage(to, text), ctx:LoginContext) => {
      val body = ProtocolNode("body", data = text.getBytes)
      val id = s"message-${WhatsApp.timestamp}-${ctx.messageCount}"
      val message = ProtocolNode("message", Map("to" -> s"$to@${WhatsApp.SERVER}", "type" -> "text", "id" -> id, "t" -> WhatsApp.timestamp), List(body))
      connector ! WriteNode(message)
      stay() using ctx.copy(messageCount = ctx.messageCount + 1, source = sender())
    }
    case Event(node @ ProtocolNode("ack", _, _, _), ctx:LoginContext) => {
      ctx.source ! MessageAck(node)
      stay()
    }

      // request avatar
    case Event(GetAvatar(to), ctx:LoginContext) => {
      val id = s"message-${WhatsApp.timestamp}-${ctx.messageCount}"
      val pic = ProtocolNode("picture", Map("type" -> "preview"))
      val node = ProtocolNode("iq", Map("id" -> id, "type" -> "get", "xmlns" -> "w:profile:picture", "to" -> s"$to@${WhatsApp.SERVER}"), List(pic))
      connector ! WriteNode(node)
      stay() using ctx.copy(messageCount = ctx.messageCount + 1, source = sender())
    }
    case Event(node @ ProtocolNode("iq", att, children, _), ctx:LoginContext) if (att.get("type") == Some("result")) && children.exists(_.tag == "picture") => {
      ctx.source ! Avatar(node)
      stay()
    }
    case Event(node @ ProtocolNode(_, _, _, _), ctx:LoginContext) => {
      log.info(s"received no-op message: $node")
      stay()
    }
  }

  private def networkFailure[T](throwTo:ActorRef):PartialFunction[Throwable,Future[T]] = {
    case ex:Throwable => {
      log.error(ex, "oops")
      throwTo ! Status.Failure(ex)
      Future.failed(ex)
    }
  }
}

object WhatsApp {
  val PORT = 443
  val DEVICE = "iPhone"
  val VERSION = "2.11.14"
  val HOST = "c.whatsapp.net"
  val SERVER = "s.whatsapp.net"

  def buildIdentity(source:String) = {
    val algo = MessageDigest.getInstance("SHA-1")
    algo.update(source.getBytes)
    val hash = algo.digest()
    val hashString = new String(hash, "iso-8859-1")
    URLEncoder.encode(hashString, "iso-8859-1").toLowerCase
  }

  def timestamp = {
    val now = new Date()
    java.lang.Long.toString(now.getTime / 1000)
  }

  //def authBlob()
}