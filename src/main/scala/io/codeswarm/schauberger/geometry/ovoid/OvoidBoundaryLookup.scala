package io.codeswarm.schauberger.geometry.ovoid

import io.codeswarm.schauberger.geometry.OvoidGeometryMath
import io.codeswarm.schauberger.math.Vector2D

/** Immutable periodic lookup table for fast ovoid boundary evaluation.
  *
  * The table stores exact samples over `[0, 2π)` and linearly interpolates
  * radius and normal between neighboring entries. Normal interpolation is
  * renormalized to keep a unit direction.
  */
final class OvoidBoundaryLookup private (val samples: Vector[OvoidBoundarySample]) {
  require(samples.size >= 32, "At least 32 lookup samples are required")

  private val size = samples.size
  private val twoPi = math.Pi * 2.0
  private val scale = size.toDouble / twoPi

  /** Returns interpolated radius for an arbitrary periodic angle. */
  def radiusAt(angle: Double): Double = interpolate(angle)._1

  /** Returns interpolated inward normal for an arbitrary periodic angle. */
  def normalAt(angle: Double): Vector2D = interpolate(angle)._2

  /** Returns interpolated radius and normal while performing angle lookup once. */
  def radiusAndNormalAt(angle: Double): (Double, Vector2D) = interpolate(angle)

  /** Maps angle to two neighboring periodic samples and interpolates them. */
  private def interpolate(angle: Double): (Double, Vector2D) = {
    val normalized = {
      val raw = angle % twoPi
      if (raw < 0.0) raw + twoPi else raw
    }
    val position = normalized * scale
    val lower = math.floor(position).toInt % size
    val upper = (lower + 1) % size
    val t = position - math.floor(position)
    val a = samples(lower)
    val b = samples(upper)
    val radius = a.radius + (b.radius - a.radius) * t
    val normal = (a.inwardNormal * (1.0 - t) + b.inwardNormal * t).normalized
    (radius, normal)
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
