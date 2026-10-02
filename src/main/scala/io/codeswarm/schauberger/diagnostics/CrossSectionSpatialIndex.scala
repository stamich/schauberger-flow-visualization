package io.codeswarm.schauberger.diagnostics

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}

/** Lightweight immutable uniform-grid index for local cross-section particle samples. */
final class CrossSectionSpatialIndex private(
                                              radius: Double,
                                              binsPerAxis: Int,
                                              buckets: Map[Int, Vector[(Vector2D, Vector3D)]]
                                            ) {
  private val span = 2.0 * radius
  private val cellSize = span / binsPerAxis.toDouble

  /** Returns samples from buckets intersecting a square search neighborhood. */
  def nearby(position: Vector2D, searchRadius: Double): Vector[(Vector2D, Vector3D)] = {
    val minX = bucketCoordinate(position.x - searchRadius)
    val maxX = bucketCoordinate(position.x + searchRadius)
    val minY = bucketCoordinate(position.y - searchRadius)
    val maxY = bucketCoordinate(position.y + searchRadius)
    val builder = Vector.newBuilder[(Vector2D, Vector3D)]
    var y = minY
    while (y <= maxY) {
      var x = minX
      while (x <= maxX) {
        builder ++= buckets.getOrElse(y * binsPerAxis + x, Vector.empty)
        x += 1
      }
      y += 1
    }
    builder.result()
  }

  /** Converts one local coordinate into a clamped bucket coordinate. */
  private def bucketCoordinate(value: Double): Int = {
    val raw = math.floor((value + radius) / cellSize).toInt
    math.max(0, math.min(binsPerAxis - 1, raw))
  }
}

/** Factory for [[CrossSectionSpatialIndex]]. */
object CrossSectionSpatialIndex {
  /** Builds an index for local particle-position/velocity pairs. */
  def build(samples: Vector[(Vector2D, Vector3D)], radius: Double, binsPerAxis: Int = 16): CrossSectionSpatialIndex = {
    require(radius > 0.0)
    require(binsPerAxis >= 2)
    val cellSize = 2.0 * radius / binsPerAxis.toDouble

    def coord(value: Double): Int = {
      val raw = math.floor((value + radius) / cellSize).toInt
      math.max(0, math.min(binsPerAxis - 1, raw))
    }

    val grouped = samples.groupBy { case (position, _) => coord(position.y) * binsPerAxis + coord(position.x) }
    new CrossSectionSpatialIndex(radius, binsPerAxis, grouped)
  }
}
