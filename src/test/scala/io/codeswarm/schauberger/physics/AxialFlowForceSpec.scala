package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}
import org.scalatest.funsuite.AnyFunSuite

/** Tests target-seeking axial acceleration. */
final class AxialFlowForceSpec extends AnyFunSuite {
  private val force = new AxialFlowForce
  private val context = FlowContext(StraightCircularPipe(100, 10), SimulationParameters.Default.copy(axialVelocity = 100, axialResponse = 2))

  test("accelerates when current axial velocity is below target") {
    assert(force.acceleration(Particle(1, Vector3D.Zero, Vector3D(50, 3, 4)), context).x > 0)
  }

  test("returns zero axial acceleration at target") {
    assert(force.acceleration(Particle(1, Vector3D.Zero, Vector3D(100, 3, 4)), context).x == 0.0)
  }
}
