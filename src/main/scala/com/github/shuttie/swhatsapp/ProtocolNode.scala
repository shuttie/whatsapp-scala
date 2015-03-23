package com.github.shuttie.swhatsapp

/**
 * Created by shutty on 3/20/15.
 */

import scala.collection.JavaConversions._

case class ProtocolNode(tag:String, attributes:Map[String,String] = Map(), children:List[ProtocolNode] = List(), data:Array[Byte] = Array()) {
  override def toString = {
    val header = (List(tag) ++ attributes.map{ case (key,value) => s"""$key="$value""""}).mkString(" ")
    val datas = BinHex.bin2hex(data)
    val childs = children.map(_.toString).mkString("\n")
    s"<$header>$datas$childs</$tag>"
  }
}

object ProtocolNode {
  def apply(tag:String, attributes:java.util.Map[String,String]) = {
    new ProtocolNode(tag, Option(attributes).map(att => Map(att.toSeq: _*)).getOrElse(Map()))
  }
  def apply(tag:String, attributes:java.util.Map[String,String], children:java.util.List[ProtocolNode]) = {
    new ProtocolNode(tag, Option(attributes).map(att => Map(att.toSeq: _*)).getOrElse(Map()), Option(children).map(_.toList).getOrElse(List()))
  }

  def apply(tag:String, attributes:java.util.Map[String,String], data:Array[Byte]) = {
    new ProtocolNode(tag, Option(attributes).map(att => Map(att.toSeq: _*)).getOrElse(Map()), List(), Option(data).getOrElse(Array[Byte]()))
  }


}
