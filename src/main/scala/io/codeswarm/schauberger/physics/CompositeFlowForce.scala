package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle

/** Combines independent acceleration strategies through vector addition.
  *
  * @param forces ordered force strategies contributing to the particle acceleration
  */
final case class CompositeFlowForce(forces: Vector[FlowForce]) {
  /** Sums all force contributions for the supplied particle and context. */
  def acceleration(particle: Particle, context: FlowContext): Vector3D =
    forces.foldLeft(Vector3D.Zero) { (sum, force) => sum + force.acceleration(particle, context) }
}
