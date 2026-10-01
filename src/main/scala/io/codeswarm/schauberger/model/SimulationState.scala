package io.codeswarm.schauberger.model

/** Immutable snapshot of the complete particle simulation.
  *
  * @param particles all particles currently present in the pipe
  * @param elapsedTime simulated time in seconds
  * @param frame number of physics updates already completed
  */
final case class SimulationState(
    particles: Vector[Particle],
    elapsedTime: Double,
    frame: Long
)
