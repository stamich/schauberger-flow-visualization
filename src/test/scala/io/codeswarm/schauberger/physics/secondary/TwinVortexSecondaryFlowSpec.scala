package io.codeswarm.schauberger.physics.secondary

import io.codeswarm.schauberger.geometry.{OvoidCrossSection, StraightPipe, TwistedPipe}
import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.SecondaryFlowParameters
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TwinVortexSecondaryFlowSpec extends AnyFunSuite {
  private val model = new TwinVortexSecondaryFlow
  private val parameters = SecondaryFlowParameters.Default

  test("untwisted geometry produces no geometry-induced secondary flow") {
    val geometry = StraightPipe(100.0, OvoidCrossSection(20.0, 30.0, 0.1))
    assert(model.targetVelocity(Vector2D(4.0, 3.0), geometry, 50.0, parameters).magnitude < 1e-12)
  }

  test("zero strength disables the model") {
    val geometry = TwistedPipe(100.0, OvoidCrossSection(20.0, 30.0, 0.1), 1.0)
    val zero = parameters.copy(strength = 0.0)
    assert(model.targetVelocity(Vector2D(4.0, 3.0), geometry, 50.0, zero) == Vector2D.Zero)
  }

  test("mirrored locations exhibit twin-vortex symmetry") {
    val geometry = TwistedPipe(100.0, OvoidCrossSection(20.0, 30.0, 0.0), 1.0)
    val left = model.targetVelocity(Vector2D(-4.0, 3.0), geometry, 50.0, parameters)
    val right = model.targetVelocity(Vector2D(4.0, 3.0), geometry, 50.0, parameters)
    assert(math.abs(left.x + right.x) < 1e-9)
    assert(math.abs(left.y - right.y) < 1e-9)
  }

  test("field is finite at many sampled positions") {
    val geometry = TwistedPipe(100.0, OvoidCrossSection(20.0, 30.0, 0.1), 1.0)
    for {
      x <- -8 to 8
      y <- -8 to 8
      point = Vector2D(x.toDouble, y.toDouble)
      if geometry.crossSection.contains(point)
    } {
      val velocity = model.targetVelocity(point, geometry, 50.0, parameters)
      assert(Seq(velocity.x, velocity.y).forall(value => !value.isNaN && !value.isInfinity))
    }
  }
}
