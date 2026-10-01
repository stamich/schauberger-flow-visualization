package io.codeswarm.schauberger.model

/** Immutable user-adjustable and numerical parameters for milestone 0.2.
  *
  * Quantities use simulation units rather than calibrated SI units. The model is an
  * educational particle visualization, not a Navier-Stokes CFD solver.
  *
  * @param particleCount number of retained tracer particles
  * @param axialVelocity target velocity along the pipe axis
  * @param axialResponse response rate toward target axial velocity
  * @param angularVelocity target solid-body angular velocity
  * @param swirlResponse response rate toward the tangential velocity requested by the swirl profile
  * @param rotationDirection clockwise or counter-clockwise swirl
  * @param wallStrength strength of soft radial wall repulsion
  * @param wallThreshold distance from the wall at which soft repulsion starts
  * @param maxVelocity absolute safety limit for particle speed
  * @param fixedTimeStep physics step in seconds
  * @param trailLength maximum number of rendered historical points per particle
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
    fixedTimeStep: Double,
    trailLength: Int
) {
  require(particleCount > 0, "particleCount must be positive")
  require(axialVelocity >= 0.0, "axialVelocity must be non-negative")
  require(axialResponse >= 0.0, "axialResponse must be non-negative")
  require(angularVelocity >= 0.0, "angularVelocity must be non-negative")
  require(swirlResponse >= 0.0, "swirlResponse must be non-negative")
  require(wallStrength >= 0.0, "wallStrength must be non-negative")
  require(wallThreshold >= 0.0, "wallThreshold must be non-negative")
  require(maxVelocity > 0.0, "maxVelocity must be positive")
  require(fixedTimeStep > 0.0, "fixedTimeStep must be positive")
  require(trailLength >= 0, "trailLength must be non-negative")
}

/** Default milestone 0.2 simulation parameters. */
object SimulationParameters {
  /** Default configuration tuned for clearly visible helical motion. */
  val Default: SimulationParameters = SimulationParameters(
    particleCount = 1500,
    axialVelocity = 120.0,
    axialResponse = 2.5,
    angularVelocity = 0.9,
    swirlResponse = 3.0,
    rotationDirection = RotationDirection.CounterClockwise,
    wallStrength = 90.0,
    wallThreshold = 18.0,
    maxVelocity = 240.0,
    fixedTimeStep = 1.0 / 120.0,
    trailLength = 60
  )
}
