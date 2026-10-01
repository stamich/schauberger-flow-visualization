package io.codeswarm.schauberger.diagnostics

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
import io.codeswarm.schauberger.model.SimulationState

/** Interpolates particle velocities onto a regular local cross-section grid.
  *
  * A compact Gaussian kernel is sufficient for the default 750-particle
  * educational visualization while keeping the implementation deterministic.
  */
final class CrossSectionVelocityFieldSampler {
  /** Samples an interpolated velocity field near one axial slice. */
  def sample(
      state: SimulationState,
      geometry: PipeGeometry,
      axialPosition: Double,
      resolution: Int,
      sliceHalfWidth: Double
  ): VectorFieldGrid = {
    require(resolution >= 3)
    require(sliceHalfWidth > 0.0)

    val radius = geometry.boundingRadius
    val step = (2.0 * radius) / (resolution - 1).toDouble
    val nearby = state.particles.filter(p => math.abs(p.position.x - axialPosition) <= sliceHalfWidth).map { particle =>
      val frame = geometry.localFrameAt(particle.position)
      val cross = frame.worldVectorToCrossSection(particle.velocity)
      val encodedLocalVelocity = Vector3D(particle.velocity.dot(frame.tangent), cross.x, cross.y)
      frame.worldPointToLocal(particle.position) -> encodedLocalVelocity
    }
    val sigma = math.max(step * 1.5, radius * 0.08)
    val sigma2 = sigma * sigma
    val spatialIndex = CrossSectionSpatialIndex.build(nearby, radius)

    val cells = Vector.tabulate(resolution * resolution) { index =>
      val ix = index % resolution
      val iy = index / resolution
      val position = Vector2D(-radius + ix * step, -radius + iy * step)
      if (!geometry.crossSection.contains(position)) VectorFieldCell(position, Vector3D.Zero, inside = false)
      else {
        var weightSum = 0.0
        var velocitySum = Vector3D.Zero
        spatialIndex.nearby(position, 3.0 * sigma).foreach { case (particlePosition, velocity) =>
          val d2 = (particlePosition - position).magnitudeSquared
          if (d2 <= 9.0 * sigma2) {
            val weight = math.exp(-d2 / (2.0 * sigma2))
            weightSum += weight
            velocitySum = velocitySum + velocity * weight
          }
        }
        val value = if (weightSum <= 1e-12) Vector3D.Zero else velocitySum / weightSum
        VectorFieldCell(position, value, inside = true)
      }
    }
    VectorFieldGrid(resolution, cells)
  }
}
