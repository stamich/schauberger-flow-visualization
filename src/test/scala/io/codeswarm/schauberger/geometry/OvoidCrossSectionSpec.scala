package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector2D
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OvoidCrossSectionSpec extends AnyFunSuite {
  private val shape = OvoidCrossSection(20, 30, 0.2)

  test("center is inside and distant points are outside") {
    assert(shape.contains(Vector2D.Zero))
    assert(!shape.contains(Vector2D(30, 30)))
  }

  test("asymmetry changes opposite vertical boundary radii") {
    assert(shape.boundaryPoint(math.Pi / 2).y > math.abs(shape.boundaryPoint(-math.Pi / 2).y))
  }

  test("clamp returns an accepted point") {
    assert(shape.contains(shape.clampInside(Vector2D(40, 10))))
  }

  test("inward normal points toward increasing signed distance") {
    val p = shape.boundaryPoint(0.3)
    val n = shape.inwardNormal(p)
    assert(shape.signedDistance(p + n * 0.01) > shape.signedDistance(p - n * 0.01))
  }
}
