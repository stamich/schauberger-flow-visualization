package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle
import org.scalatest.funsuite.AnyFunSuite

/** Tests semi-implicit Euler integration in three dimensions. */
final class SemiImplicitEulerIntegratorSpec extends AnyFunSuite {
  test("updates velocity before position") {
    val result = new SemiImplicitEulerIntegrator().integrate(
      Particle(1, Vector3D.Zero, Vector3D(10, 0, 0)),
      Vector3D(2, 3, 4),
      1.0,
      100.0
    )
    assert(result.velocity == Vector3D(12, 3, 4))
    assert(result.position == Vector3D(12, 3, 4))
  }

  test("caps final velocity magnitude") {
    val result = new SemiImplicitEulerIntegrator().integrate(
      Particle(1, Vector3D.Zero, Vector3D.Zero), Vector3D(100, 0, 0), 1.0, 5.0)
    assert(math.abs(result.velocity.magnitude - 5.0) < 1e-12)
  }
}
