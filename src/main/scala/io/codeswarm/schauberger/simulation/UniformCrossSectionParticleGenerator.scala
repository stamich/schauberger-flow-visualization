package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}

import scala.util.Random

/** Deterministic rejection sampler that works with every [[PipeGeometry]] cross-section.
 *
 * @param seed pseudo-random seed used to keep demos, tests and benchmarks reproducible
 */
final class UniformCrossSectionParticleGenerator(seed: Long) extends ParticleGenerator {
  private val random = new Random(seed)

  /** Generates exactly `count` particles distributed through the pipe volume. */
  override def generate(
                         count: Int,
                         geometry: PipeGeometry,
                         parameters: SimulationParameters
                       ): Vector[Particle] = {
    require(count > 0)
    Vector.tabulate(count) { index =>
      val x = random.nextDouble() * geometry.length
      Particle(
        id = index.toLong,
        position = geometry.fromLocalCrossSection(x, randomLocalPoint(geometry)),
        velocity = Vector3D(parameters.axial.velocity, 0.0, 0.0)
      )
    }
  }

  /** Reuses an existing particle id and respawns it at x = 0 inside the current shape. */
  override def respawn(
                        particle: Particle,
                        geometry: PipeGeometry,
                        parameters: SimulationParameters
                      ): Particle =
    particle.copy(
      position = geometry.fromLocalCrossSection(0.0, randomLocalPoint(geometry)),
      velocity = Vector3D(parameters.axial.velocity, 0.0, 0.0)
    )

  /** Samples inside a bounding square until the shape accepts a point with a small wall margin. */
  private def randomLocalPoint(geometry: PipeGeometry): Vector2D = {
    val bound = geometry.boundingRadius * 0.94
    var point = Vector2D.Zero
    var accepted = false
    while (!accepted) {
      point = Vector2D(
        (random.nextDouble() * 2.0 - 1.0) * bound,
        (random.nextDouble() * 2.0 - 1.0) * bound
      )
      accepted = geometry.crossSection.contains(point) &&
        geometry.crossSection.signedDistance(point) >= geometry.boundingRadius * 0.03
    }
    point
  }
}
