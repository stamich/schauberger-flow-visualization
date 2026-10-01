package io.codeswarm.schauberger.diagnostics

import io.codeswarm.schauberger.math.Vector2D

/** One scalar diagnostic value in local cross-section coordinates. */
final case class ScalarFieldCell(position: Vector2D, value: Double, inside: Boolean)
