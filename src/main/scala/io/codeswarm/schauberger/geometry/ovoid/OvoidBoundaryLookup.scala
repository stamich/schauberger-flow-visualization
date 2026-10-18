package io.codeswarm.schauberger.geometry.ovoid

import io.codeswarm.schauberger.geometry.OvoidGeometryMath
import io.codeswarm.schauberger.math.Vector2D

/** Immutable periodic lookup table for fast ovoid boundary evaluation.
  *
  * The table stores exact samples over `[0, 2π)` and linearly interpolates
  * radius and normal between neighboring entries. Normal interpolation is
  * renormalized to keep a unit direction.
  *
  * The values used on the hot path are copied into primitive arrays, so a
  * radius lookup allocates nothing and a normal lookup allocates only its
  * result; the boxed [[samples]] stay available for inspection.
  */
final class OvoidBoundaryLookup private (val samples: Vector[OvoidBoundarySample]) {
  require(samples.size >= 32, "At least 32 lookup samples are required")

  private val size = samples.size
  private val twoPi = math.Pi * 2.0
  private val scale = size.toDouble / twoPi
  private val radii: Array[Double] = samples.iterator.map(_.radius).toArray
  private val normalX: Array[Double] = samples.iterator.map(_.inwardNormal.x).toArray
  private val normalY: Array[Double] = samples.iterator.map(_.inwardNormal.y).toArray

  /** Returns interpolated radius for an arbitrary periodic angle. */
  def radiusAt(angle: Double): Double = {
    val position = positionOf(angle)
    val lower = lowerIndex(position)
    val t = position - math.floor(position)
    radii(lower) + (radii((lower + 1) % size) - radii(lower)) * t
  }

  /** Returns interpolated inward normal for an arbitrary periodic angle. */
  def normalAt(angle: Double): Vector2D = {
    val position = positionOf(angle)
    normalFrom(lowerIndex(position), position - math.floor(position))
  }

  /** Returns interpolated radius and normal from a single index computation. */
  def rayAt(angle: Double): BoundaryRay = {
    val position = positionOf(angle)
    val lower = lowerIndex(position)
    val t = position - math.floor(position)
    BoundaryRay(radii(lower) + (radii((lower + 1) % size) - radii(lower)) * t, normalFrom(lower, t))
  }

  /** Returns interpolated radius and normal as a pair. */
  def radiusAndNormalAt(angle: Double): (Double, Vector2D) = {
    val ray = rayAt(angle)
    (ray.radius, ray.inwardNormal)
  }

  /** Maps a periodic angle to a fractional table position in `[0, size)`. */
  private def positionOf(angle: Double): Double = {
    val raw = angle % twoPi
    (if (raw < 0.0) raw + twoPi else raw) * scale
  }

  private def lowerIndex(position: Double): Int = math.floor(position).toInt % size

  /** Interpolates and renormalizes the normal between entries `lower` and `lower + 1`. */
  private def normalFrom(lower: Int, t: Double): Vector2D = {
    val upper = (lower + 1) % size
    Vector2D(
      normalX(lower) * (1.0 - t) + normalX(upper) * t,
      normalY(lower) * (1.0 - t) + normalY(upper) * t
    ).normalized
  }
}

/** Factory for immutable ovoid boundary lookup tables. */
object OvoidBoundaryLookup {
  /** Precomputes exact samples for the supplied ovoid dimensions. */
  def build(
      semiWidth: Double,
      semiHeight: Double,
      asymmetry: Double,
      sampleCount: Int
  ): OvoidBoundaryLookup = {
    require(sampleCount >= 32)
    val twoPi = math.Pi * 2.0
    val samples = Vector.tabulate(sampleCount) { index =>
      val angle = twoPi * index.toDouble / sampleCount.toDouble
      val radius = OvoidGeometryMath.radius(angle, semiWidth, semiHeight, asymmetry)
      val derivative = OvoidGeometryMath.radiusDerivative(angle, semiWidth, semiHeight, asymmetry)
      val normal = OvoidGeometryMath.inwardNormal(angle, semiWidth, semiHeight, asymmetry)
      OvoidBoundarySample(angle, radius, derivative, normal)
    }
    new OvoidBoundaryLookup(samples)
  }
}
