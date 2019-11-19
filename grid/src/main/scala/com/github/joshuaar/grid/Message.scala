package com.github.joshuaar.grid

sealed trait Message

case class Request(n: Int) extends Message

case class Response(stats: RollingStats) extends Message

case class Terminate() extends Message
