package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}

import scala.util.Random

/** Generates particles near the inlet with a uniform vertical distribution.
  *
  * @param seed random seed; injecting it keeps tests and resets reproducible
  */
final class UniformInletParticleGenerator(seed: Long = 0L) extends ParticleGenerator {

  /** Generates particles with stable sequential IDs and small inlet-position jitter. */
  override def generate(
      count: Int,
      geometry: PipeGeometry,
      parameters: SimulationParameters
  ): Vector[Particle] = {
    require(count > 0, "count must be positive")
    val random = new Random(seed)
    val safeRadius = geometry.radius * 0.92
    Vector.tabulate(count) { index =>
      val y = -safeRadius + random.nextDouble() * safeRadius * 2.0
      val x = random.nextDouble() * math.min(geometry.length * 0.03, 20.0)
      Particle(
        id = index.toLong,
        position = Vector2D(x, y),
        velocity = Vector2D(parameters.axialVelocity * 0.85, 0.0)
      )
    }
  }
}
