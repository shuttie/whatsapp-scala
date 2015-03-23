package com.github.shuttie.swhatsapp.protocol

import java.io.ByteArrayOutputStream

import com.github.shuttie.swhatsapp.{KeyStream, Pbkdf2}

/**
 * Created by shutty on 3/23/15.
 */
case class Auth(keys:List[Array[Byte]], response:Array[Byte], inputKey:KeyStream, outputKey:KeyStream) {
}

object Auth {
  def apply(cellPhone:String, password:String, challenge:Array[Byte]) = {
    val keys = generateKeys(password, challenge)
    val inputKey = new KeyStream(keys(2), keys(3))
    val outputKey = new KeyStream(keys(0), keys(1))
    val stream = new ByteArrayOutputStream()
    stream.write(cellPhone.getBytes)
    stream.write(challenge)
    val response = outputKey.encode(stream.toByteArray, 0, 0, stream.size())
    new Auth(keys, response, inputKey, outputKey)
  }

  private def generateKeys(password:String, challenge:Array[Byte]):List[Array[Byte]] = {
    (for (i <- 0 until 4) yield {
      val stream = new ByteArrayOutputStream()
      stream.write(challenge)
      stream.write(i + 1)
      val decodedPassword = org.apache.commons.codec.binary.Base64.decodeBase64(password.getBytes)
      Pbkdf2.encrypt("SHA-1", decodedPassword, stream.toByteArray, 2, 20, true)
    }).toList
  }

}