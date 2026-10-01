package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.math.Vector2D

/** One local vector-field sample used by the cross-section renderer. */
final case class FieldSample(position: Vector2D, velocity: Vector2D)
