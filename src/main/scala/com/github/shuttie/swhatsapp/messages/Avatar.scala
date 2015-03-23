package com.github.shuttie.swhatsapp.messages

import com.github.shuttie.swhatsapp.ProtocolNode

/**
 * Created by shutty on 3/23/15.
 */
case class Avatar(cellPhone:String, picture:Option[Array[Byte]])

object Avatar {
  def apply(node:ProtocolNode) = {
    new Avatar(
      cellPhone = node.attributes.getOrElse("from", throw new IllegalArgumentException("no from field")),
      picture = node.children.headOption.map(_.data)
    )
  }
}