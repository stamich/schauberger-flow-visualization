package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SemiImplicitEulerIntegratorSpec extends AnyFunSuite {
  test("integrator updates velocity before position") {
    val p = (new SemiImplicitEulerIntegrator).integrate(Particle(1, Vector3D.Zero, Vector3D(10, 0, 0)), Vector3D(2, 0, 0), 1, 100)
    assert(p.velocity.x == 12 && p.position.x == 12)
  }
}
