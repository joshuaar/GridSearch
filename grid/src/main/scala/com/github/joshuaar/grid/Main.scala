package com.github.joshuaar.grid

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Main {
  //default parameters
  val numThreads = 3
  val seed = 0L
  val dim = 3
  val delta = 0.001

  val usage =
    """
      | Usage: gridsearch [--num-worker-threads num] [--delta num] [--seed num] [--dim num]
    """.stripMargin

  def main(args: Array[String]) = {

    val argsParsed = try {
      parseArgs(Map(), args.toList)
    } catch {
      case e: MatchError => {
        println("improper command")
        println(usage)
        System.exit(1)
        null
      }
    }

    val system = ActorSystem.create("GridSearchSystem",
      ConfigFactory.parseString("akka.log-dead-letters-during-shutdown=off\nakka.log-dead-letters=0"))

    val coordinator = system.actorOf(Props(new GridSearchCoordinator(
      getAs[Int](argsParsed, "numThreads", numThreads),
      getAs[Long](argsParsed, "seed", seed),
      getAs[Int](argsParsed, "dim", dim),
      getAs[Double](argsParsed, "delta", delta)
    )))
  }

  def getAs[A](map: Map[String, Any], key: String, default: A) = {
    map.get(key).asInstanceOf[Option[A]].getOrElse(default)
  }

  private def parseArgs(map: Map[String, Any], args: List[String]): Map[String, Any] = {
    args match {
      case Nil => map
      case "--num-worker-threads" :: value :: tail => parseArgs(map ++ Map(("numThreads" -> value.toInt)), tail)
      case "--delta" :: value :: tail => parseArgs(map ++ Map(("delta" -> value.toDouble)), tail)
      case "--seed" :: value :: tail => parseArgs(map ++ Map(("seed" -> value.toLong)), tail)
      case "--dim" :: value :: tail => parseArgs(map ++ Map(("dim" -> value.toInt)), tail)
      case "--help" :: tail => {
        println(usage)
        System.exit(0)
        null
      }
    }
  }

}
