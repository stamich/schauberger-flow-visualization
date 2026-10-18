package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector2D

/** Distance to the boundary and the inward normal for one local point, obtained together.
 *
 * Asking for both separately repeats the polar-angle computation and the
 * boundary lookup; the simulation needs both for every particle on every step.
 *
 * @param signedDistance positive inside the section, negative outside
 * @param inwardNormal   inward-pointing unit normal, or zero on the axis
 */
final case class BoundaryQuery(signedDistance: Double, inwardNormal: Vector2D)
