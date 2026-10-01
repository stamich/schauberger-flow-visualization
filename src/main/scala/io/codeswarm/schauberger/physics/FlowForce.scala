package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.Particle

/** Produces an acceleration contribution for a particle.
  *
  * New force models such as the milestone 0.2 swirl force should implement this
  * trait instead of modifying the simulation engine.
  */
trait FlowForce {

  /** Calculates this force model's acceleration contribution.
    *
    * @param particle particle being evaluated
    * @param context immutable simulation context
    * @return acceleration in simulation units per second squared
    */
  def acceleration(particle: Particle, context: FlowContext): Vector2D
}
