package com.github.joshuaar.grid

//Rolling Mean and Variance calulation from https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance

trait Stats {
  def n: Int
  def mu: Double
  def variance: Double

  //combining means and variances as in https://www.emathzone.com/tutorials/basic-statistics/combined-variance.html
  //[Nx*((Nx-1)Sx2-Sy2)+Ny*((Ny-1)*Sy2-Sx2)+NxNy(Sx2+Sy2+(Mx-My)^2)]/[(Nx+Ny-1)*(Nx+Ny)]
  def combine(o: Stats) = {

    val Nx = n
    val Sx2 = variance
    val Sy2 = o.variance
    val Ny = o.n
    val Mx = mu
    val My = o.mu

    //val sc = (Nx*((Nx-1)*Sx2-Sy2)+Ny*((Ny-1)*Sy2-Sx2)+Nx*Ny*(Sx2+Sy2+math.pow(Mx-My, 2)))/((Nx+Ny-1)*(Nx+Ny))

    val xc = ((n*mu) + (o.n*o.mu)) / (n+o.n)

    val sc = (n*variance + o.n*o.variance + (n*math.pow(mu - xc,2)) + (o.n*math.pow(o.mu-xc,2))) / (n+o.n)

    val nc = n + o.n
    new Stats {
      override val n: Int = nc
      override val mu: Double = xc
      override val variance: Double = sc
      }
  }
}

case class RollingStats(override val n: Int, override val mu: Double, M2: Double, override val variance: Double) extends Stats {
  def next(x: Int) = {
    if (n == 0) {
      RollingStats(1, x, 0, 0)
    }
    else {
      val n2 = n + 1
      val mu2 = mu + ((x - mu) / n2)
      val M22 = M2 + ((x - mu) * (x - mu2))
      val variance2 = M22 / (n2 - 1)
      RollingStats(n2, mu2, M22, variance2)
    }
  }
}

