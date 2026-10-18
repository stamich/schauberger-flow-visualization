package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.{CircularCrossSection, OvoidCrossSection, StraightPipe}
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters, SimulationState}
import io.codeswarm.schauberger.physics.secondary.TwinVortexSecondaryFlow
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FlowMetricsCalculatorSpec extends AnyFunSuite {
  test("centerline particle has zero normalized radial metric") {
    val geometry = StraightPipe(100.0, CircularCrossSection(10.0))
    val state = SimulationState(Vector(Particle(1L, Vector3D(10.0, 0.0, 0.0), Vector3D(5.0, 0.0, 0.0))), 0.0, 0L)
    val metrics = new FlowMetricsCalculator(new TwinVortexSecondaryFlow).calculate(state, geometry, SimulationParameters.Default)
    assert(metrics.meanNormalizedRadialPosition == 0.0)
    assert(metrics.meanAxialVelocity == 5.0)
    assert(metrics.meanSecondaryVelocity == 0.0)
  }

  test("particles in each third of a circular section are counted in that third") {
    val geometry = StraightPipe(100.0, CircularCrossSection(10.0))
    val particles = Vector(1.0, 5.0, 9.0).zipWithIndex.map { case (radius, index) =>
      Particle(index.toLong, Vector3D(10.0, radius, 0.0), Vector3D.Zero)
    }
    val metrics = new FlowMetricsCalculator(new TwinVortexSecondaryFlow)
      .calculate(SimulationState(particles, 0.0, 0L), geometry, SimulationParameters.Default)
    assert(math.abs(metrics.occupancy.inner - 1.0 / 3.0) < 1e-12)
    assert(math.abs(metrics.occupancy.middle - 1.0 / 3.0) < 1e-12)
    assert(math.abs(metrics.occupancy.outer - 1.0 / 3.0) < 1e-12)
  }

  test("an area-uniform ovoid population occupies roughly 11, 33 and 56 percent of the thirds") {
    val geometry = StraightPipe(300.0, OvoidCrossSection(80.0, 100.0, 0.14))
    val parameters = SimulationParameters.Default.copy(particleCount = 6000)
    val state = new UniformCrossSectionParticleGenerator(7L).generate(parameters.particleCount, geometry, parameters)
    val occupancy = new FlowMetricsCalculator(new TwinVortexSecondaryFlow)
      .calculate(SimulationState(state, 0.0, 0L), geometry, parameters).occupancy
    // The generator keeps a small wall margin, so the outer third is slightly lighter than the ideal 56%.
    assert(math.abs(occupancy.inner - 0.111) < 0.03)
    assert(math.abs(occupancy.middle - 0.333) < 0.04)
    assert(occupancy.outer > 0.50 && occupancy.outer < 0.60)
  }
}
