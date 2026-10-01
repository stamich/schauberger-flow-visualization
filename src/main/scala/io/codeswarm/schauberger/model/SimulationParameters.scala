package io.codeswarm.schauberger.model

/** User-adjustable and numerical parameters of the milestone 0.1 simulation.
  *
  * All quantities are expressed in simulation units rather than calibrated SI units.
  *
  * @param particleCount number of particles retained by the simulation
  * @param axialVelocity target velocity along the pipe x axis
  * @param axialResponse rate at which particles approach the target axial velocity
  * @param wallStrength strength of the soft wall-repulsion force
  * @param wallThreshold distance from a wall at which repulsion starts
  * @param maxVelocity absolute speed limit used for numerical safety
  * @param fixedTimeStep simulation step size in seconds
  */
final case class SimulationParameters(
    particleCount: Int,
    axialVelocity: Double,
    axialResponse: Double,
    wallStrength: Double,
    wallThreshold: Double,
    maxVelocity: Double,
    fixedTimeStep: Double
) {
  require(particleCount > 0, "particleCount must be positive")
  require(axialVelocity >= 0.0, "axialVelocity must be non-negative")
  require(axialResponse >= 0.0, "axialResponse must be non-negative")
  require(wallStrength >= 0.0, "wallStrength must be non-negative")
  require(wallThreshold >= 0.0, "wallThreshold must be non-negative")
  require(maxVelocity > 0.0, "maxVelocity must be positive")
  require(fixedTimeStep > 0.0, "fixedTimeStep must be positive")
}

/** Default parameters chosen for a clear interactive visualization. */
object SimulationParameters {
  /** Default milestone 0.1 configuration. */
  val Default: SimulationParameters = SimulationParameters(
    particleCount = 1500,
    axialVelocity = 120.0,
    axialResponse = 2.5,
    wallStrength = 90.0,
    wallThreshold = 18.0,
    maxVelocity = 220.0,
    fixedTimeStep = 1.0 / 120.0
  )
}
