package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.SecondaryFlowParameters
import io.codeswarm.schauberger.physics.secondary.SecondaryFlowModel

/** Samples a secondary-flow model on a regular local cross-section grid. */
final class SecondaryFlowFieldSampler(model: SecondaryFlowModel) {
  /** Returns only samples lying inside the active cross-section. */
  def sample(
              geometry: PipeGeometry,
              axialPosition: Double,
              parameters: SecondaryFlowParameters,
              resolution: Int
            ): Vector[FieldSample] = {
    require(resolution >= 3)
    val radius = geometry.boundingRadius
    val step = (2.0 * radius) / (resolution - 1).toDouble
    Vector.tabulate(resolution * resolution) { index =>
      val ix = index % resolution
      val iy = index / resolution
      Vector2D(-radius + ix * step, -radius + iy * step)
    }.filter(geometry.crossSection.contains).map { point =>
      FieldSample(point, model.targetVelocity(point, geometry, axialPosition, parameters))
    }
  }
}
