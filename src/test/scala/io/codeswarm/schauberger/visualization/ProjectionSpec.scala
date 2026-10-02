package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ProjectionSpec extends AnyFunSuite {
  test("longitudinal projection maps x-y") {
    assert(new LongitudinalProjection().project(Vector3D(1, 2, 3)) == Vector2D(1, 2))
  }

  test("cross-section projection maps y-z") {
    assert(new CrossSectionProjection().project(Vector3D(1, 2, 3)) == Vector2D(2, 3))
  }
}
