package io.codeswarm.schauberger.model

/** Share of particles in the inner, middle and outer third of the normalized radius.
 *
 * The normalized radius is 0 at the cross-section center and 1 at the wall, so
 * the thirds are comparable for circular and ovoid sections. Particles spread
 * uniformly over the area occupy about 11%, 33% and 56% of the thirds, which makes
 * this metric a direct detector for inward or outward drift.
 *
 * @param inner  fraction of particles with normalized radius below 1/3
 * @param middle fraction of particles with normalized radius from 1/3 up to 2/3
 * @param outer  fraction of particles with normalized radius of at least 2/3
 */
final case class RadialOccupancy(inner: Double, middle: Double, outer: Double) {
  require(inner >= 0.0 && middle >= 0.0 && outer >= 0.0)
}

/** Factory helpers for [[RadialOccupancy]]. */
object RadialOccupancy {
  /** Occupancy of an empty population. */
  val Empty: RadialOccupancy = RadialOccupancy(0.0, 0.0, 0.0)

  /** Normalizes particle counts into fractions, returning [[Empty]] when there are no particles. */
  def fromCounts(inner: Long, middle: Long, outer: Long): RadialOccupancy = {
    require(inner >= 0L && middle >= 0L && outer >= 0L)
    val total = inner + middle + outer
    if (total == 0L) Empty
    else RadialOccupancy(inner.toDouble / total, middle.toDouble / total, outer.toDouble / total)
  }

  /** Classifies one normalized radius into the matching third. */
  def thirdOf(normalizedRadius: Double): Int =
    if (normalizedRadius < 1.0 / 3.0) 0 else if (normalizedRadius < 2.0 / 3.0) 1 else 2
}
