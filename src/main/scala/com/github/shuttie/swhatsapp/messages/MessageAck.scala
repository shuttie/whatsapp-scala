package com.github.shuttie.swhatsapp.messages

import com.github.shuttie.swhatsapp.ProtocolNode

/**
 * Created by shutty on 3/23/15.
 */
case class MessageAck(from:String, id:String)

object MessageAck {
  def apply(node:ProtocolNode) = {
    new MessageAck(
      from = node.attributes.getOrElse("from", throw new IllegalArgumentException("no from field")),
      id = node.attributes.getOrElse("id", throw new IllegalArgumentException("no from field"))
    )
  }
}