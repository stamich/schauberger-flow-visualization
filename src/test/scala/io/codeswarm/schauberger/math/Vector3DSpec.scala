package io.codeswarm.schauberger.math

import org.scalatest.funsuite.AnyFunSuite

/** Unit tests for three-dimensional vector algebra. */
final class Vector3DSpec extends AnyFunSuite {
  test("addition and subtraction are component-wise") {
    assert(Vector3D(1, 2, 3) + Vector3D(4, 5, 6) == Vector3D(5, 7, 9))
    assert(Vector3D(4, 5, 6) - Vector3D(1, 2, 3) == Vector3D(3, 3, 3))
  }

  test("normalization produces a unit vector") {
    assert(math.abs(Vector3D(3, 4, 0).normalized.magnitude - 1.0) < 1e-12)
  }

  test("cross product follows the right-hand rule") {
    assert(Vector3D(1, 0, 0).cross(Vector3D(0, 1, 0)) == Vector3D(0, 0, 1))
  }

  test("cross product is orthogonal to both operands") {
    val a = Vector3D(2, -1, 3)
    val b = Vector3D(0.5, 4, -2)
    val c = a.cross(b)
    assert(math.abs(c.dot(a)) < 1e-10)
    assert(math.abs(c.dot(b)) < 1e-10)
  }

  test("limit preserves direction and caps magnitude") {
    val limited = Vector3D(10, 0, 0).limit(3)
    assert(limited == Vector3D(3, 0, 0))
  }
}
