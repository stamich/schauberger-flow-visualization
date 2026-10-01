package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle
/** Sums independent acceleration strategies. */
final case class CompositeFlowForce(forces: Vector[FlowForce]) {
  def acceleration(particle: Particle, context: FlowContext): Vector3D = forces.foldLeft(Vector3D.Zero)((sum, force) => sum + force.acceleration(particle, context))
}
