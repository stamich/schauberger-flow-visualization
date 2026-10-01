package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.Particle

/** Drives horizontal velocity toward a target instead of accelerating forever.
  *
  * The force uses a first-order response:
  * `a_x = response * (targetVelocity - currentVelocity)`.
  */
final class AxialFlowForce extends FlowForce {

  /** Calculates acceleration required to approach the configured target x velocity. */
  override def acceleration(particle: Particle, context: FlowContext): Vector2D = {
    val deltaVelocity = context.parameters.axialVelocity - particle.velocity.x
    Vector2D(deltaVelocity * context.parameters.axialResponse, 0.0)
  }
}
