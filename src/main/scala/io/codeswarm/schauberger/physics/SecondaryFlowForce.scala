package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle
import io.codeswarm.schauberger.physics.secondary.SecondaryFlowModel

/** Drives local cross-sectional velocity toward a geometry-induced target field. */
final class SecondaryFlowForce(model: SecondaryFlowModel) extends FlowForce {
  /** Computes the world-space acceleration corresponding to the local model target. */
  override def acceleration(particle: Particle, context: FlowContext): Vector3D = {
    val parameters = context.parameters.secondaryFlow
    if (!parameters.enabled) Vector3D.Zero
    else {
      val geometry = context.geometry
      val localPosition = geometry.toLocalCrossSection(particle.position)
      val targetLocal = model.targetVelocity(localPosition, geometry, particle.position.x, parameters)
      val frame = geometry.localFrameAt(particle.position)
      val targetWorld = frame.normal * targetLocal.x + frame.binormal * targetLocal.y
      val currentLocalU = particle.velocity.dot(frame.normal)
      val currentLocalV = particle.velocity.dot(frame.binormal)
      val currentWorld = frame.normal * currentLocalU + frame.binormal * currentLocalV
      (targetWorld - currentWorld) * parameters.response
    }
  }
}
