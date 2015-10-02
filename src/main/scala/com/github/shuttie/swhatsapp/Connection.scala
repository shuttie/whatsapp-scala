package com.github.shuttie.swhatsapp

import java.net.InetSocketAddress

import akka.actor.{ActorRef, LoggingFSM}
import akka.io.{Tcp, IO}
import akka.util.ByteString
import com.github.shuttie.swhatsapp.messages.ConnectSuccessful
import com.github.shuttie.swhatsapp.protocol.Auth

import scala.annotation.tailrec
import scala.collection.mutable

/**
 * Created by shutty on 3/20/15.
 */
sealed trait NetState
case object Disconnected extends NetState
case object Active extends NetState
sealed trait NetContext
case object NoContext extends NetContext
case class ConnectingContext(source:ActorRef) extends NetContext
case class ActiveContext(source:ActorRef, conn:ActorRef, buffer:Array[Byte] = Array()) extends NetContext

case class Connect(host:String, port:Int)
case class WriteBytes(data:Array[Byte])
case class WriteNode(node:ProtocolNode)
case class Read(nodes:List[ProtocolNode])
case class UpgradeKeys(auth:Auth)

class Connection extends LoggingFSM[NetState,NetContext] {
  implicit val implicitSystem = context.system
  val io = IO(Tcp)
  val reader = new BinTreeNodeReader
  val writer = new BinTreeNodeWriter

  startWith(Disconnected, NoContext)

  when(Disconnected) {
    case Event(Connect(host, port), _) => {
      log.info(s"connecting to $host:$port")
      io ! Tcp.Connect(new InetSocketAddress(host, port))
      goto(Disconnected) using ConnectingContext(sender())
    }
    case Event(Tcp.Connected(remote, local), ctx:ConnectingContext) => {
      val socket = sender()
      socket ! Tcp.Register(self)
      log.info(s"Connected to $remote from $local")
      ctx.source ! ConnectSuccessful
      goto(Active) using ActiveContext(ctx.source, socket)
    }
    case Event(msg @ _, _) => {
      log.error(s"Unknown event: $msg")
      stay()
    }
  }

  when(Active) {
    case Event(UpgradeKeys(auth), ctx:ActiveContext) => {
      reader.setKey(auth.inputKey)
      writer.setKey(auth.outputKey)
      log.info("Encryption upgraded")
      stay()
    }
    case Event(WriteBytes(data), ctx:ActiveContext) => {
      log.info(s"writing ${data.size} bytes to socket")
      ctx.conn ! Tcp.Write(ByteString.fromArray(data))
      stay()
    }
    case Event(WriteNode(node), ctx:ActiveContext) => {
      val bytes = writer.write(node, true)
      log.info(s"writing ${bytes.size} bytes to socket for node $node")
      ctx.conn ! Tcp.Write(ByteString.fromArray(bytes))
      stay()
    }
    case Event(Tcp.Received(data:ByteString), ctx:ActiveContext) => {
      val currentBuffer = ctx.buffer ++ data
      val parsed = parseNode(currentBuffer)
      parsed._2.foreach(node => ctx.source ! node)
      goto(Active) using ctx.copy(buffer = parsed._1)
    }
    case Event(x @ _, _) => {
      val br = x
      stay()
    }
  }

  def parseResponseSize(header:Array[Byte]):Int = {
    if (header.isEmpty) {
      0
    } else {
      var treeLength = ((header(0) & 0x0f) & 0xFF) << 16
      treeLength += (header(1) & 0xFF) << 8
      treeLength += (header(2) & 0xFF) << 0
      treeLength + 3
    }
  }

  @tailrec
  private def parseNode(data:Array[Byte], nodes:List[ProtocolNode] = List()):(Array[Byte],List[ProtocolNode]) = {
    val requiredSize = parseResponseSize(data)
    log.info(s"response size = $requiredSize, data size = ${data.length}")
    if ((requiredSize == 0) || (data.length < requiredSize)) {
      log.info(s"parsed all avaliable ${nodes.size} nodes, ${data.length} bytes left in buffer")
      data -> nodes
    } else {
      val nodeBuffer = data.slice(0, requiredSize)
      val node = reader.nextTree(nodeBuffer)
      val parsedNodes = if (node != null) nodes ++ List(node) else nodes
      parseNode(data.drop(requiredSize), parsedNodes)
    }
  }

}
