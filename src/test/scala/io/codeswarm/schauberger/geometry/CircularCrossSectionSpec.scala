package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector2D
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class CircularCrossSectionSpec extends AnyFunSuite {
  private val shape=CircularCrossSection(10)
  test("contains center and rejects outside") { assert(shape.contains(Vector2D.Zero)); assert(!shape.contains(Vector2D(11,0))) }
  test("signed distance is positive inside") { assert(math.abs(shape.signedDistance(Vector2D(7,0))-3)<1e-9) }
  test("clamp projects inside") { assert(shape.contains(shape.clampInside(Vector2D(20,0)))) }
}
