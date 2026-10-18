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

  private def errors(samples: Int): (Double, Double) = {
    val exact = new ExactOvoidGeometryKernel(110.0, 125.0, 0.14)
    val lookup = new LookupOvoidGeometryKernel(OvoidBoundaryLookup.build(110.0, 125.0, 0.14, samples))
    var radius = 0.0
    var normal = 0.0
    (0 until 20000).foreach { i =>
      val angle = math.Pi * 2.0 * (i + 0.37) / 20000.0
      radius = math.max(radius, math.abs(lookup.radiusAt(angle) - exact.radiusAt(angle)) / exact.radiusAt(angle))
      val dot = math.max(-1.0, math.min(1.0, exact.normalAt(angle).dot(lookup.normalAt(angle))))
      normal = math.max(normal, math.toDegrees(math.acos(dot)))
    }
    (radius, normal)
  }

  test("the 512-sample default keeps radius error under 0.002% and normal error under 0.002 degrees") {
    val (radius, normal) = errors(512)
    assert(radius < 2e-5)
    assert(normal < 0.002)
  }

  test("error shrinks as the table grows") {
    val (r256, n256) = errors(256)
    val (r1024, n1024) = errors(1024)
    assert(r1024 < r256 && n1024 < n256)
  }

  test("a single ray lookup equals separate radius and normal lookups") {
    val lookup = new LookupOvoidGeometryKernel(OvoidBoundaryLookup.build(110.0, 125.0, 0.14, 512))
    (-700 to 700).foreach { i =>
      val angle = i * 0.0173
      val ray = lookup.rayAt(angle)
      assert(ray.radius == lookup.radiusAt(angle))
      assert(ray.inwardNormal == lookup.normalAt(angle))
    }
  }

  test("the exact kernel's default ray matches its radius and normal") {
    val exact = new ExactOvoidGeometryKernel(110.0, 125.0, 0.14)
    val ray = exact.rayAt(1.3)
    assert(ray.radius == exact.radiusAt(1.3) && ray.inwardNormal == exact.normalAt(1.3))
  }
}
