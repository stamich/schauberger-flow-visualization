package io.codeswarm.schauberger.diagnostics

/** Summary produced from sampled tangential vortex velocities.
 *
 * Velocities are signed: positive turns with the configured rotation
 * direction, negative against it.
 *
 * @param samples         sampled signed profile
 * @param peakVelocity    signed velocity at the sample of largest magnitude
 * @param peakRadius      normalized radius of that sample
 * @param zeroCrossings   normalized radii where the velocity changes sign, in increasing order
 * @param reverseVelocity most negative sampled velocity, or zero when the profile never reverses
 */
final case class VortexProfileDiagnostics(
                                           samples: Vector[RadialProfileSample],
                                           peakVelocity: Double,
                                           peakRadius: Double,
                                           zeroCrossings: Vector[Double] = Vector.empty,
                                           reverseVelocity: Double = 0.0
                                         ) {
  /** True when the profile turns in both senses. */
  def isCounterRotating: Boolean = zeroCrossings.nonEmpty
}
