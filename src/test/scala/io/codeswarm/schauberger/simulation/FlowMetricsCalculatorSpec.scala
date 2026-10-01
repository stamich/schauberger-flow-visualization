package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.{CircularCrossSection, StraightPipe}
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
}
