package io.codeswarm.schauberger.model

/** Immutable physical state for one simulation instant. */
final case class SimulationState(particles: Vector[Particle], elapsedTime: Double, frame: Long)
