package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.Particle
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

/** Numerical tests for the baseline integrator. */
@RunWith(classOf[JUnitRunner])
final class SemiImplicitEulerIntegratorSpec extends AnyFunSuite {
  private val integrator = new SemiImplicitEulerIntegrator

  test("velocity is updated before position") {
    val particle = Particle(1L, Vector2D.Zero, Vector2D(10.0, 0.0))
    val result = integrator.integrate(particle, Vector2D(2.0, 0.0), 1.0)
    assert(result.velocity == Vector2D(12.0, 0.0))
    assert(result.position == Vector2D(12.0, 0.0))
  }

  test("zero delta time leaves particle unchanged") {
    val particle = Particle(1L, Vector2D(1.0, 2.0), Vector2D(3.0, 4.0))
    assert(integrator.integrate(particle, Vector2D(5.0, 6.0), 0.0) == particle)
  }
}
