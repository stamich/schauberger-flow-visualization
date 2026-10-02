package io.codeswarm.schauberger.geometry.ovoid

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LookupOvoidGeometryKernelSpec extends AnyFunSuite {
  test("1024-sample lookup stays close to exact radius and normal") {
    val exact = new ExactOvoidGeometryKernel(110.0, 125.0, 0.14)
    val lookup = new LookupOvoidGeometryKernel(OvoidBoundaryLookup.build(110.0, 125.0, 0.14, 1024))
    var maxRelativeRadiusError = 0.0
    var maxNormalErrorDegrees = 0.0

    (0 until 10000).foreach { i =>
      val angle = math.Pi * 2.0 * (i + 0.37) / 10000.0
      val exactRadius = exact.radiusAt(angle)
      val lookupRadius = lookup.radiusAt(angle)
      maxRelativeRadiusError = math.max(maxRelativeRadiusError, math.abs(lookupRadius - exactRadius) / exactRadius)
      val dot = math.max(-1.0, math.min(1.0, exact.normalAt(angle).dot(lookup.normalAt(angle))))
      maxNormalErrorDegrees = math.max(maxNormalErrorDegrees, math.toDegrees(math.acos(dot)))
    }

    assert(maxRelativeRadiusError < 0.001)
    assert(maxNormalErrorDegrees < 0.5)
  }
}
