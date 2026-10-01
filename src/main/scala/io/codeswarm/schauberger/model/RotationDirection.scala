package io.codeswarm.schauberger.model

/** Direction of rotation when looking in the positive x direction. */
sealed trait RotationDirection {
  /** Sign multiplier used by tangential-direction calculations. */
  def sign: Double
}

/** Supported vortex rotation directions. */
object RotationDirection {
  /** Clockwise rotation when looking from inlet toward outlet. */
  case object Clockwise extends RotationDirection { override val sign: Double = -1.0 }

  /** Counter-clockwise rotation when looking from inlet toward outlet. */
  case object CounterClockwise extends RotationDirection { override val sign: Double = 1.0 }
}
