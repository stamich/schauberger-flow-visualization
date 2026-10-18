package io.codeswarm.schauberger.diagnostics

/** Minimum and maximum scalar values used to normalize a heat map. */
final case class FieldRange(min: Double, max: Double) {
  /** Normalizes a value to `[0,1]`, handling constant fields safely. */
  def normalize(value: Double): Double =
    if (max - min <= 1e-12) 0.5 else math.max(0.0, math.min(1.0, (value - min) / (max - min)))

  /** Returns the range centered on zero that contains this one, so zero maps to the middle of a diverging palette. */
  def symmetric: FieldRange = {
    val bound = math.max(math.abs(min), math.abs(max))
    FieldRange(-bound, bound)
  }
}

/** Factory helpers for [[FieldRange]]. */
object FieldRange {
  /** Computes a finite range for a collection of values. */
  def from(values: Vector[Double]): FieldRange =
    if (values.isEmpty) FieldRange(0.0, 0.0) else FieldRange(values.min, values.max)
}
