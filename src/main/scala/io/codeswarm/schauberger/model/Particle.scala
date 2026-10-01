package io.codeswarm.schauberger.model

import io.codeswarm.schauberger.math.Vector3D

/** Immutable visual tracer carried by the simulated flow.
  *
  * A particle is a tracer, not a literal water molecule. Milestone 0.2 deliberately
  * does not attach pressure, density, temperature or mass to individual particles.
  *
  * @param id stable identifier retained across outlet-to-inlet respawns
  * @param position current three-dimensional world position
  * @param velocity current three-dimensional velocity in simulation units per second
  */
final case class Particle(id: Long, position: Vector3D, velocity: Vector3D)
