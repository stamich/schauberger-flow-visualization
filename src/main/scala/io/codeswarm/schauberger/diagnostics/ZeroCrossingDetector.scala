package io.codeswarm.schauberger.diagnostics

/** Finds radii where a sampled signed profile changes sign. */
object ZeroCrossingDetector {
  /** Relative size below which a sample counts as exactly zero. */
  private val RelativeZero = 1e-12

  /** Returns the normalized radii of all sign changes, in increasing order.
   *
   * A crossing between two samples of opposite sign is placed by linear
   * interpolation. Samples that are zero (such as the axis) are not crossings
   * by themselves; a zero between two opposite signs is reported at that
   * sample. A profile that only touches zero reports nothing.
   */
  def detect(samples: Seq[RadialProfileSample]): Vector[Double] = {
    val scale = if (samples.isEmpty) 0.0 else samples.iterator.map(s => math.abs(s.tangentialVelocity)).max
    val threshold = scale * RelativeZero
    val crossings = Vector.newBuilder[Double]
    var previous: Option[RadialProfileSample] = None
    var firstZeroAfterPrevious: Option[RadialProfileSample] = None
    samples.foreach { sample =>
      val value = sample.tangentialVelocity
      if (math.abs(value) <= threshold) {
        if (previous.isDefined && firstZeroAfterPrevious.isEmpty) firstZeroAfterPrevious = Some(sample)
      } else {
        previous.foreach { before =>
          if (before.tangentialVelocity * value < 0.0) {
            crossings += firstZeroAfterPrevious.map(_.normalizedRadius).getOrElse {
              val t = before.tangentialVelocity / (before.tangentialVelocity - value)
              before.normalizedRadius + t * (sample.normalizedRadius - before.normalizedRadius)
            }
          }
        }
        previous = Some(sample)
        firstZeroAfterPrevious = None
      }
    }
    crossings.result()
  }
}
