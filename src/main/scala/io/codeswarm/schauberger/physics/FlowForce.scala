package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle
/** Strategy calculating one acceleration contribution. */
trait FlowForce { def acceleration(particle: Particle, context: FlowContext): Vector3D }
