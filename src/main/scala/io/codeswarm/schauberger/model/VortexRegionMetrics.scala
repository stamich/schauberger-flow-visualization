package io.codeswarm.schauberger.model

/** How the particle population divides between the two rotation senses.
 *
 * A signed mean angular velocity cancels when a counter-rotating vortex turns
 * both ways, so it cannot tell a healthy counter-rotating flow from no swirl at
 * all. These figures keep the two senses apart.
 *
 * @param forwardShare           fraction of particles turning with the configured direction
 * @param reverseShare           fraction turning against it
 * @param quiescentShare         fraction with negligible angular velocity
 * @param meanForwardAngular     mean angular velocity of the forward particles, positive, zero if none
 * @param meanReverseAngular     mean angular velocity of the reverse particles, negative, zero if none
 * @param meanAbsoluteAngular    mean magnitude of the angular velocity over all particles
 */
final case class VortexRegionMetrics(
                                      forwardShare: Double,
                                      reverseShare: Double,
                                      quiescentShare: Double,
                                      meanForwardAngular: Double,
                                      meanReverseAngular: Double,
                                      meanAbsoluteAngular: Double
                                    ) {
  /** True when a meaningful part of the population turns each way. */
  def isCounterRotating(minimumShare: Double = 0.02): Boolean =
    forwardShare >= minimumShare && reverseShare >= minimumShare
}

/** Helpers for [[VortexRegionMetrics]]. */
object VortexRegionMetrics {
  /** Metrics of an empty population. */
  val Empty: VortexRegionMetrics = VortexRegionMetrics(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

  /** Builds metrics from per-class counts and angular-velocity sums. */
  def fromAccumulators(
                        forwardCount: Long,
                        reverseCount: Long,
                        quiescentCount: Long,
                        forwardSum: Double,
                        reverseSum: Double,
                        absoluteSum: Double
                      ): VortexRegionMetrics = {
    val total = forwardCount + reverseCount + quiescentCount
    if (total == 0L) Empty
    else VortexRegionMetrics(
      forwardShare = forwardCount.toDouble / total,
      reverseShare = reverseCount.toDouble / total,
      quiescentShare = quiescentCount.toDouble / total,
      meanForwardAngular = if (forwardCount == 0L) 0.0 else forwardSum / forwardCount,
      meanReverseAngular = if (reverseCount == 0L) 0.0 else reverseSum / reverseCount,
      meanAbsoluteAngular = absoluteSum / total
    )
  }
}
