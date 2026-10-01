package io.codeswarm.schauberger.model

/** Immutable physical/numerical parameters for milestone 0.3.
  *
  * Values use simulation units. Defaults are intentionally calmer than 0.2 so the
  * first frame remains readable with the new geometry-driven rendering.
  */
final case class SimulationParameters(
    particleCount: Int,
    axialVelocity: Double,
    axialResponse: Double,
    angularVelocity: Double,
    swirlResponse: Double,
    rotationDirection: RotationDirection,
    wallStrength: Double,
    wallThreshold: Double,
    maxVelocity: Double,
    fixedTimeStep: Double
) {
  require(particleCount > 0)
  require(axialVelocity >= 0.0 && axialResponse >= 0.0)
  require(angularVelocity >= 0.0 && swirlResponse >= 0.0)
  require(wallStrength >= 0.0 && wallThreshold >= 0.0)
  require(maxVelocity > 0.0 && fixedTimeStep > 0.0)
}

/** Default physical settings. */
object SimulationParameters {
  val Default: SimulationParameters = SimulationParameters(
    particleCount = 750,
    axialVelocity = 90.0,
    axialResponse = 2.2,
    angularVelocity = 0.55,
    swirlResponse = 2.0,
    rotationDirection = RotationDirection.CounterClockwise,
    wallStrength = 85.0,
    wallThreshold = 16.0,
    maxVelocity = 210.0,
    fixedTimeStep = 1.0 / 120.0
  )
}
