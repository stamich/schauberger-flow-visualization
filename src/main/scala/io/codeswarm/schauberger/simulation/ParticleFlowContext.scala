package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.model.Particle
import io.codeswarm.schauberger.physics.FlowContext

/** Complete per-particle context passed to flow-force strategies. */
final case class ParticleFlowContext(
    particle: Particle,
    flow: FlowContext,
    geometry: ParticleGeometryContext
)
