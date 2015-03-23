package com.github.shuttie.swhatsapp

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.net.{InetSocketAddress, URLEncoder}
import java.security.MessageDigest


import akka.actor.{Props, Status, ActorRef, LoggingFSM}
import akka.io.{IO, Tcp}
import akka.util.{Timeout, ByteString}
import com.github.shuttie.swhatsapp.messages.{ConnectSuccessful, LoginFailed, LoginRequest}
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
    case Event(LoginRequest(uid,password,identity,nick), Uninitialized) => {
      val id = WhatsApp.buildIdentity(identity)
      log.debug(s"Using identity: $id")
      connector ! Connect(WhatsApp.HOST, WhatsApp.PORT)
      goto(Connecting) using LoginContext(null, sender(), uid, password, id, nick)
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

  //def authBlob()
}