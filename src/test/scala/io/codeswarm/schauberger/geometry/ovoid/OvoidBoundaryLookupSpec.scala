package io.codeswarm.schauberger.geometry.ovoid

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OvoidBoundaryLookupSpec extends AnyFunSuite {
  private val lookup = OvoidBoundaryLookup.build(110.0, 125.0, 0.14, 1024)

  test("lookup is periodic across the zero/two-pi seam") {
    val a = lookup.radiusAt(1e-8)
    val b = lookup.radiusAt(math.Pi * 2.0 + 1e-8)
    assert(math.abs(a - b) < 1e-9)
  }

  test("interpolated normals stay normalized") {
    (0 until 1000).foreach { i =>
      val normal = lookup.normalAt(math.Pi * 2.0 * (i + 0.37) / 1000.0)
      assert(math.abs(normal.magnitude - 1.0) < 1e-9)
    }
  }
}
