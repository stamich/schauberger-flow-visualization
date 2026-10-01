package io.codeswarm.schauberger.diagnostics

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}

/** One interpolated velocity sample in a local cross-section grid. */
final case class VectorFieldCell(position: Vector2D, velocity: Vector3D, inside: Boolean)
