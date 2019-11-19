package com.github.joshuaar.grid

import akka.actor.{Actor, ActorRef, PoisonPill, Props}

import scala.concurrent.Await
import scala.util.Random
import scala.concurrent.duration._

class GridSearchCoordinator(threads: Int, seed: Long, dim: Int = 3, delta: Double = 0.001) extends Actor {

  private val rng = new Random(seed)
  //configuration keys (would be factored into a configuration object in a production app)
  private val shutdownTimeout = 1 seconds
  private val terminationCheckInterval = 10000
  private val Z95 = 1.96
  private val t0 = System.currentTimeMillis

  print(s"starting coordinator with ${threads} workers")
  println("")

  private val children: Seq[ActorRef] = for (i <- 0 until threads)
    yield context.actorOf(Props(new GridSearchWorker(context.self, rng.nextLong(), dim)), name = s"Child$i")

  private var stats: Map[String, Stats] = Map()

  for (i <- children) {
    i ! Request(terminationCheckInterval)
  }

  override def receive: Receive = {
    case Response(x) => {
      val workerName = context.sender().path.name
      val newStats: Stats = stats.get(workerName).map(i => i.combine(x)).getOrElse(x)
      stats += (workerName -> newStats)
      val combinedStats = calculateStats(stats)
      if (terminationConditionMet(combinedStats))
        terminate()
      else
        sender() ! Request(terminationCheckInterval)
    }
  }

  private def calculateStats(stats: Map[String, Stats]): Stats = {
    if (stats.size > 1) {
      stats.values.reduce((a,b) => a.combine(b))
    }
    else {
      stats.head._2
    }
  }

  //Using method from https://quant.stackexchange.com/questions/21764/stopping-monte-carlo-simulation-once-certain-convergence-level-is-reached
  private def terminationConditionMet(cs: Stats): Boolean = {
    val sigma = math.sqrt(cs.variance)
    val nsqrt = math.sqrt(cs.n)
    val Z = Z95
    val muEst = cs.mu

    val re = ((sigma/nsqrt) * Z) / muEst

    prettyPrint(cs, sigma, re)

    if (cs.n < 1000)
      false
    else if (re < delta) {
      true
    }
    else {
      false
    }
  }

  private def prettyPrint(cs: Stats, sigma: Double, error: Double) = {
    print(s"Mean Path Length: ${"%01.3f".format(cs.mu)}, " +
      s"Standard Deviation: ${"%01.3f".format(sigma)}, Error: ${"%01.5f".format(error)} \r")
  }

  private def terminate(): Unit = {
    println("")
    println(s"finished in ${System.currentTimeMillis - t0} milliseconds")
    context.system.stop(context.self)
    Await.result(context.system.terminate(), shutdownTimeout)
  }
}
