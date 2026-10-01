package io.codeswarm.schauberger.diagnostics

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CrossSectionSpatialIndexSpec extends AnyFunSuite {
  test("nearby returns local samples and excludes distant buckets") {
    val near = Vector2D(0.0, 0.0) -> Vector3D(1.0, 0.0, 0.0)
    val far = Vector2D(9.0, 9.0) -> Vector3D(2.0, 0.0, 0.0)
    val index = CrossSectionSpatialIndex.build(Vector(near, far), radius = 10.0, binsPerAxis = 10)
    val result = index.nearby(Vector2D.Zero, searchRadius = 1.5)
    assert(result.contains(near))
    assert(!result.contains(far))
  }
}
