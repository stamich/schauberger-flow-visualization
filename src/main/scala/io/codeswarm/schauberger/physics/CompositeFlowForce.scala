package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.Particle

/** Combines multiple independent force models by summing their accelerations.
  *
  * @param forces force models evaluated for every particle
  */
final case class CompositeFlowForce(forces: Vector[FlowForce]) {

  /** Calculates total acceleration from all configured forces. */
  def acceleration(particle: Particle, context: FlowContext): Vector2D =
    forces.foldLeft(Vector2D.Zero) { (total, force) =>
      total + force.acceleration(particle, context)
    }
}
