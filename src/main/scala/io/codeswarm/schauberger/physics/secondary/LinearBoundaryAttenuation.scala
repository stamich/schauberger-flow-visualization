package io.codeswarm.schauberger.physics.secondary

/** Linear no-slip-like attenuation that fades the model field to zero at the wall. */
case object LinearBoundaryAttenuation extends BoundaryAttenuation {
  /** Computes the clamped linear attenuation factor. */
  override def factor(signedDistance: Double, fadeDistance: Double): Double = {
    if (fadeDistance <= 0.0) {
      if (signedDistance > 0.0) 1.0 else 0.0
    } else math.max(0.0, math.min(1.0, signedDistance / fadeDistance))
  }
}
