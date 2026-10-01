package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, SimulationState}
import org.scalatest.funsuite.AnyFunSuite

/** Tests aggregate axial, tangential and angular metrics. */
final class FlowMetricsCalculatorSpec extends AnyFunSuite {
  test("calculates signed tangential and angular velocity") {
    val state = SimulationState(
      Vector(Particle(1, Vector3D(10, 5, 0), Vector3D(100, 0, 10))),
      0.0,
      0
    )
    val metrics = new FlowMetricsCalculator().calculate(state, StraightCircularPipe(100, 10))
    assert(math.abs(metrics.meanAxialVelocity - 100.0) < 1e-12)
    assert(math.abs(metrics.meanTangentialVelocity - 10.0) < 1e-12)
    assert(math.abs(metrics.meanAngularVelocity - 2.0) < 1e-12)
    assert(math.abs(metrics.vorticityProxy - 4.0) < 1e-12)
  }
}
