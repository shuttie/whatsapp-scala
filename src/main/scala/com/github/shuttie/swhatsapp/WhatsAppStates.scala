package com.github.shuttie.swhatsapp

import java.io.ByteArrayOutputStream

import akka.actor.ActorRef

/**
 * Created by shutty on 3/20/15.
 */
sealed trait State
case object Idle extends State
case object Connecting extends State
case object LoggingIn extends State
case object LoggedIn extends State

sealed trait Context
case object Uninitialized extends Context
case class LoginContext(socket:ActorRef, source:ActorRef, uid:String, password:String, buffer:ByteArrayOutputStream = new ByteArrayOutputStream()) extends Context