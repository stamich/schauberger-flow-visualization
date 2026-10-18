package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.{CircularCrossSection, OvoidCrossSection, StraightPipe}
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, RotationDirection, SimulationParameters, SimulationState}
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

  private val circular = StraightPipe(100.0, CircularCrossSection(10.0))
  private def metricsOf(velocities: Seq[Double], parameters: SimulationParameters = SimulationParameters.Default) = {
    // Particles sit on the local y axis, so a velocity along z is tangential and positive z is counter-clockwise.
    val particles = velocities.zipWithIndex.map { case (vz, i) => Particle(i.toLong, Vector3D(10.0, 5.0, 0.0), Vector3D(0.0, 0.0, vz)) }.toVector
    new FlowMetricsCalculator(new TwinVortexSecondaryFlow).calculate(SimulationState(particles, 0.0, 0L), circular, parameters)
  }

  test("particles turning both ways are counted in both regions instead of cancelling") {
    // Default swirl is 0.55 rad/s; at radius 5 the angular velocity is vz / 5.
    val metrics = metricsOf(Seq(2.0, 2.0, 2.0, -2.0))
    assert(metrics.regions.forwardShare == 0.75 && metrics.regions.reverseShare == 0.25)
    assert(math.abs(metrics.regions.meanForwardAngular - 0.4) < 1e-12)
    assert(math.abs(metrics.regions.meanReverseAngular + 0.4) < 1e-12)
    assert(math.abs(metrics.regions.meanAbsoluteAngular - 0.4) < 1e-12)
    assert(metrics.regions.isCounterRotating())
  }

  test("a pure counter-rotating population has a zero signed mean but a non-zero absolute mean") {
    val metrics = metricsOf(Seq(2.0, -2.0))
    assert(math.abs(metrics.meanAngularVelocity) < 1e-12)
    assert(metrics.regions.meanAbsoluteAngular > 0.3)
  }

  test("region sense follows the configured rotation direction") {
    val base = SimulationParameters.Default
    val clockwise = base.copy(swirl = base.swirl.copy(rotationDirection = RotationDirection.Clockwise))
    assert(metricsOf(Seq(-2.0), clockwise).regions.forwardShare == 1.0)
    assert(metricsOf(Seq(2.0), clockwise).regions.reverseShare == 1.0)
  }

  test("slow particles are quiescent and empty states have empty region metrics") {
    assert(metricsOf(Seq(0.0, 0.01)).regions.quiescentShare == 1.0)
    val empty = new FlowMetricsCalculator(new TwinVortexSecondaryFlow).calculate(SimulationState(Vector.empty, 0.0, 0L), circular, SimulationParameters.Default)
    assert(empty.regions == io.codeswarm.schauberger.model.VortexRegionMetrics.Empty)
  }
}
