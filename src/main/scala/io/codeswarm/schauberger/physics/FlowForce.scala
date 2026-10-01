package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.simulation.ParticleFlowContext

/** Strategy calculating one acceleration contribution from a precomputed particle context. */
trait FlowForce {
  /** Returns this strategy's acceleration contribution in world coordinates. */
  def acceleration(context: ParticleFlowContext): Vector3D
}
