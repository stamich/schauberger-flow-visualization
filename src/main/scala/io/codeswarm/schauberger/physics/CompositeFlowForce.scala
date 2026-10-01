package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.simulation.ParticleFlowContext

/** Sums independent acceleration strategies. */
final case class CompositeFlowForce(forces: Vector[FlowForce]) {
  /** Calculates total acceleration for one precomputed particle context. */
  def acceleration(context: ParticleFlowContext): Vector3D =
    forces.foldLeft(Vector3D.Zero)((sum, force) => sum + force.acceleration(context))
}
