package com.github.joshuaar.grid

import org.scalatest._

class RollingStatsTest extends FlatSpec with Matchers {

  "A Rolling Stats" should "compute mean and variance correctly" in {
    var rs = RollingStats(0,0,0,0)
    rs = rs.next(1)
    rs = rs.next(2)
    rs = rs.next(3)
    rs = rs.next(4)

    rs.mu should be (2.5)
    rs.variance should be  > 1.666
    rs.variance should be  < 1.667
  }

  "A Stats" should "combine two stats placeholders correctly with approximately correct " +
    "variance and be numerically stable" in {

    var stats: Stats = null
    for (i <- 1 to 10) {
      var rs = RollingStats(0,0,0,0)
      for (i <- 1 to 100) {
        rs = rs.next(i)
      }
      if (stats == null) {
        stats = rs
      } else {
        stats = stats.combine(rs)
      }
    }
    stats.mu should be (50.5)
    stats.variance should be > 840.0
    stats.variance should be < 845.0
    stats.n should be (1000)
  }
}
