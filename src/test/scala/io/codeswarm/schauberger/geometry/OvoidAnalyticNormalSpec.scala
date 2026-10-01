package io.codeswarm.schauberger.geometry

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OvoidAnalyticNormalSpec extends AnyFunSuite {
  test("analytic normal points inward from a boundary sample") {
    val shape = OvoidCrossSection(20.0, 30.0, 0.2)
    val angle = 0.8
    val boundary = shape.boundaryPoint(angle)
    val inward = shape.inwardNormal(boundary)
    val inside = boundary + inward * 0.1
    assert(shape.contains(inside))
    assert(math.abs(inward.magnitude - 1.0) < 1e-8)
  }

  test("analytic normal is finite around the complete ovoid") {
    val shape = OvoidCrossSection(20.0, 30.0, -0.15)
    (0 until 100).foreach { i =>
      val point = shape.boundaryPoint(2.0 * math.Pi * i / 100.0)
      val normal = shape.inwardNormal(point)
      assert(normal.x.isFinite && normal.y.isFinite)
    }
  }
}
