package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
import org.scalatest.funsuite.AnyFunSuite

/** Tests longitudinal and cross-section coordinate projections. */
final class ProjectionSpec extends AnyFunSuite {
  test("longitudinal projection maps x and y") {
    assert(new LongitudinalProjection().project(Vector3D(1, 2, 3)) == Vector2D(1, 2))
  }

  test("cross-section projection maps y and z") {
    assert(new CrossSectionProjection().project(Vector3D(1, 2, 3)) == Vector2D(2, 3))
  }
}
