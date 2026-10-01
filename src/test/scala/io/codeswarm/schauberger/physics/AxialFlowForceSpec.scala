package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.{CircularCrossSection, StraightPipe}
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AxialFlowForceSpec extends AnyFunSuite {
  test("force accelerates toward target axial velocity") {
    val pipe = StraightPipe(100.0, CircularCrossSection(10.0))
    val particle = Particle(1L, Vector3D(10.0, 0.0, 0.0), Vector3D(20.0, 0.0, 0.0))
    val base = SimulationParameters.Default
    val parameters = base.copy(axial = base.axial.copy(velocity = 100.0))
    val acceleration = (new AxialFlowForce).acceleration(PhysicsTestSupport.context(particle, pipe, parameters))
    assert(acceleration.x > 0.0)
    assert(math.abs(acceleration.y) < 1e-9)
    assert(math.abs(acceleration.z) < 1e-9)
  }
}
