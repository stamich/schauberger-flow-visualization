package io.codeswarm.schauberger.geometry.ovoid

import io.codeswarm.schauberger.math.Vector2D

/** One precomputed ovoid boundary sample used by [[OvoidBoundaryLookup]]. */
final case class OvoidBoundarySample(
                                      angle: Double,
                                      radius: Double,
                                      radiusDerivative: Double,
                                      inwardNormal: Vector2D
                                    )
