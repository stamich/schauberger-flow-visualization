package io.codeswarm.schauberger.model

/** Immutable complete physical state of the simulation.
  *
  * @param particles current tracer particles
  * @param elapsedTime accumulated simulated seconds
  * @param frame number of completed physics steps
  */
final case class SimulationState(
    particles: Vector[Particle],
    elapsedTime: Double,
    frame: Long
)
