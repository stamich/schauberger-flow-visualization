package io.codeswarm.schauberger.physics.secondary

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.SecondaryFlowParameters

/** Neutral model useful for regression checks and disabled secondary flow. */
case object NoSecondaryFlow extends SecondaryFlowModel {
  /** Always returns zero local velocity. */
  override def targetVelocity(
      localPosition: Vector2D,
      geometry: PipeGeometry,
      axialPosition: Double,
      parameters: SecondaryFlowParameters
  ): Vector2D = Vector2D.Zero
}
