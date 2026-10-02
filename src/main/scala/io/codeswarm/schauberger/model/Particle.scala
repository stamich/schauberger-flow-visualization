package io.codeswarm.schauberger.model

import io.codeswarm.schauberger.math.Vector3D

/** Immutable tracer particle carrying only identity, position and velocity. */
final case class Particle(id: Long, position: Vector3D, velocity: Vector3D)
