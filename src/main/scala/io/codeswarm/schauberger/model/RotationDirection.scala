package io.codeswarm.schauberger.model

/** Direction of swirl around the local pipe tangent. */
sealed trait RotationDirection { def sign: Double }
/** Supported swirl directions. */
object RotationDirection {
  case object CounterClockwise extends RotationDirection { val sign: Double = 1.0 }
  case object Clockwise extends RotationDirection { val sign: Double = -1.0 }
}
