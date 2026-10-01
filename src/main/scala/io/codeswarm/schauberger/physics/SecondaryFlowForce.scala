package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.physics.secondary.SecondaryFlowModel
import io.codeswarm.schauberger.simulation.ParticleFlowContext

/** Drives cross-sectional velocity toward a geometry-induced secondary-flow target. */
final class SecondaryFlowForce(model: SecondaryFlowModel) extends FlowForce {
  /** Computes acceleration from the cached local frame and local particle position. */
  override def acceleration(context: ParticleFlowContext): Vector3D = {
    val parameters = context.flow.parameters.secondaryFlow
    if (!parameters.enabled) Vector3D.Zero
    else {
      val targetLocal = model.targetVelocity(
        context.geometry.localPosition,
        context.flow.geometry,
        context.particle.position.x,
        parameters
      )
      val frame = context.geometry.frame
      val targetWorld = frame.crossSectionVectorToWorld(targetLocal)
      val currentLocal = frame.worldVectorToCrossSection(context.particle.velocity)
      val currentWorld = frame.crossSectionVectorToWorld(currentLocal)
      (targetWorld - currentWorld) * parameters.response
    }
  }
}
