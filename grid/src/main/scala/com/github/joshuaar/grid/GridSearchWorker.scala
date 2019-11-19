package com.github.joshuaar.grid

import akka.actor.{Actor, ActorRef}

import scala.annotation.tailrec
import scala.util.Random

class GridSearchWorker(parent: ActorRef, seed: Long, dim: Int = 3) extends Actor {

  type Coordinates = Array[Boolean]

  val numGen = new Random(seed)

  val finalPosition: Coordinates = (for (i <- 0 until dim) yield true).toArray

  @tailrec
  private def walk(from: Coordinates, to: Coordinates = finalPosition, walkLength: Int = 0): Int = {
    val indexToMutate = numGen.nextInt(dim)
    from(indexToMutate) = !from(indexToMutate)
    val nextWalkLength = walkLength + 1
    if (from.sameElements(to))
      nextWalkLength
    else
      walk(from, to, nextWalkLength)
  }

  private def doWalk(): Int = {
    val initialPosition: Coordinates = (for (i <- 0 until dim) yield false).toArray
    walk(initialPosition, finalPosition)
  }

  private def doWalks(n: Int): RollingStats = {
    var stats = RollingStats(0,0,0,0)
    for (i <- 0 until n) {
      stats = stats.next(doWalk())
    }
    stats
  }

  override def receive: Receive = {
    case x: Request => {
      val res = Response(doWalks(x.n))
      parent ! res
    }
  }
}
