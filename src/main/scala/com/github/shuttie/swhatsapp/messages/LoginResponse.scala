package com.github.shuttie.swhatsapp.messages

/**
 * Created by shutty on 3/20/15.
 */
sealed trait LoginResponse
case class LoginFailed(message:String) extends LoginResponse
case object LoginSuccessful extends LoginResponse