package io.codeswarm.schauberger.math

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

/** Unit tests for immutable vector mathematics. */
@RunWith(classOf[JUnitRunner])
final class Vector2DSpec extends AnyFunSuite {

  test("addition is component-wise") {
    assert(Vector2D(1.0, 2.0) + Vector2D(3.0, 4.0) == Vector2D(4.0, 6.0))
  }

  test("subtraction is component-wise") {
    assert(Vector2D(5.0, 7.0) - Vector2D(2.0, 3.0) == Vector2D(3.0, 4.0))
  }

  test("scalar multiplication and division preserve direction") {
    val vector = Vector2D(3.0, 4.0)
    assert(vector * 2.0 == Vector2D(6.0, 8.0))
    assert(vector / 2.0 == Vector2D(1.5, 2.0))
  }

  test("division by zero is rejected") {
    assertThrows[IllegalArgumentException](Vector2D(1.0, 2.0) / 0.0)
  }

  test("magnitude follows Pythagorean distance") {
    assert(math.abs(Vector2D(3.0, 4.0).magnitude - 5.0) < 1e-12)
  }

  test("zero vector normalizes safely to zero") {
    assert(Vector2D.Zero.normalized == Vector2D.Zero)
  }

  test("normalization produces unit magnitude") {
    assert(math.abs(Vector2D(3.0, 4.0).normalized.magnitude - 1.0) < 1e-12)
  }

  test("limit caps magnitude without changing smaller vectors") {
    assert(Vector2D(3.0, 4.0).limit(10.0) == Vector2D(3.0, 4.0))
    assert(math.abs(Vector2D(30.0, 40.0).limit(10.0).magnitude - 10.0) < 1e-12)
  }

  test("dot product is calculated correctly") {
    assert(Vector2D(1.0, 2.0).dot(Vector2D(3.0, 4.0)) == 11.0)
  }
}
