package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.math.Vector2D

/** Decomposition of one particle velocity in the local pipe frame. */
final case class VelocityComponents(
    axial: Double,
    tangential: Double,
    radial: Double,
    crossSection: Vector2D
)
