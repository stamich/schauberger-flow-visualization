package io.codeswarm.schauberger.model

import io.codeswarm.schauberger.math.Vector2D

/** Immutable particle representing a small visual element of the modelled flow.
  *
  * The particle is deliberately not a fluid molecule and carries no pressure,
  * density, or thermodynamic state in milestone 0.1.
  *
  * @param id stable identity used across outlet-to-inlet respawns
  * @param position current position in simulation/world coordinates
  * @param velocity current velocity in simulation units per second
  */
final case class Particle(id: Long, position: Vector2D, velocity: Vector2D)
