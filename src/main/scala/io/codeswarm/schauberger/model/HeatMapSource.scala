package io.codeswarm.schauberger.model

/** Where the velocity grid behind the heat map comes from. */
sealed trait HeatMapSource {
  /** Name shown in the UI. */
  def displayName: String
}

/** Supported heat-map sources. */
object HeatMapSource {
  /** Gaussian interpolation of the simulated particles; shows what the simulation actually does. */
  case object Particles extends HeatMapSource { val displayName = "Particles" }

  /** The analytic swirl profile evaluated on the grid; the noise-free reference the particles should follow. */
  case object AnalyticProfile extends HeatMapSource { val displayName = "Analytic profile" }

  /** Stable order used by the UI. */
  val values: Vector[HeatMapSource] = Vector(Particles, AnalyticProfile)

  /** Resolves a UI display value, defaulting to the particle source. */
  def fromDisplayName(name: String): HeatMapSource = values.find(_.displayName == name).getOrElse(Particles)
}
