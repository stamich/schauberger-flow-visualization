package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}

import scala.util.Random

/** Deterministic uniform-area inlet generator for a circular cross-section.
  *
  * Radius is sampled with sqrt(U) so particles are uniform in area rather than
  * over-concentrated near the center.
  *
  * @param seed pseudo-random seed used for reproducible runs and tests
  */
final class UniformInletParticleGenerator(seed: Long) extends ParticleGenerator {
  private val random = new Random(seed)

  /** Generates particles along the pipe with uniformly distributed cross-section positions. */
  override def generate(
      count: Int,
      geometry: PipeGeometry,
      parameters: SimulationParameters
  ): Vector[Particle] = {
    require(count > 0, "count must be positive")
    Vector.tabulate(count) { index =>
      val radial = randomCrossSectionPoint(geometry.radius * 0.92)
      val x = random.nextDouble() * geometry.length
      Particle(
        id = index.toLong,
        position = Vector3D(x, radial._1, radial._2),
        velocity = Vector3D(parameters.axialVelocity, 0.0, 0.0)
      )
    }
  }

  /** Repositions an existing particle near the inlet while preserving its identifier. */
  override def respawn(
      particle: Particle,
      geometry: PipeGeometry,
      parameters: SimulationParameters
  ): Particle = {
    val radial = randomCrossSectionPoint(geometry.radius * 0.92)
    particle.copy(
      position = Vector3D(0.0, radial._1, radial._2),
      velocity = Vector3D(parameters.axialVelocity, 0.0, 0.0)
    )
  }

  /** Samples a point uniformly by area inside a circle. */
  private def randomCrossSectionPoint(maxRadius: Double): (Double, Double) = {
    val radius = math.sqrt(random.nextDouble()) * maxRadius
    val angle = random.nextDouble() * math.Pi * 2.0
    (radius * math.cos(angle), radius * math.sin(angle))
  }
}
