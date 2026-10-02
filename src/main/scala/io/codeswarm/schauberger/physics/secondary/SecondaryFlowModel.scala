package io.codeswarm.schauberger.physics.secondary

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.SecondaryFlowParameters

/** Produces a target cross-sectional velocity field induced by pipe geometry. */
trait SecondaryFlowModel {
  /** Calculates target velocity in local cross-section coordinates. */
  def targetVelocity(
                      localPosition: Vector2D,
                      geometry: PipeGeometry,
                      axialPosition: Double,
                      parameters: SecondaryFlowParameters
                    ): Vector2D
}
