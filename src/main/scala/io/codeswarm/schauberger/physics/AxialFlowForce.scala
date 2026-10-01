package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle

/** Drives axial velocity toward a configurable target instead of accelerating forever. */
final class AxialFlowForce extends FlowForce {
  /** Applies first-order response a_x = response * (target - current). */
  override def acceleration(particle: Particle, context: FlowContext): Vector3D = {
    val delta = context.parameters.axialVelocity - particle.velocity.x
    Vector3D(delta * context.parameters.axialResponse, 0.0, 0.0)
  }
}
