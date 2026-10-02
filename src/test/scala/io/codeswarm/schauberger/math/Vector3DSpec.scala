package io.codeswarm.schauberger.math

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class Vector3DSpec extends AnyFunSuite {
  test("cross product follows the right-hand rule") {
    assert(Vector3D.UnitX.cross(Vector3D.UnitY) == Vector3D.UnitZ)
  }

  test("normalization produces unit magnitude") {
    assert(math.abs(Vector3D(3, 4, 0).normalized.magnitude - 1.0) < 1e-9)
  }

  test("limit caps magnitude") {
    assert(math.abs(Vector3D(10, 0, 0).limit(2).magnitude - 2.0) < 1e-9)
  }
}
