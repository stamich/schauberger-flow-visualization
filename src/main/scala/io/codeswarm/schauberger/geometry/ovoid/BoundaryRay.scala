package io.codeswarm.schauberger.geometry.ovoid

import io.codeswarm.schauberger.math.Vector2D

/** Boundary radius and inward normal along one polar ray. */
final case class BoundaryRay(radius: Double, inwardNormal: Vector2D)
