package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector2D
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

/** Unit tests for milestone 0.1 straight pipe geometry. */
@RunWith(classOf[JUnitRunner])
final class StraightCircularPipeSpec extends AnyFunSuite {
  private val pipe = StraightCircularPipe(length = 100.0, radius = 10.0)

  test("contains accepts points inside all boundaries") {
    assert(pipe.contains(Vector2D(50.0, 0.0)))
    assert(pipe.contains(Vector2D(0.0, -10.0)))
    assert(pipe.contains(Vector2D(100.0, 10.0)))
  }

  test("contains rejects points outside axial or wall boundaries") {
    assert(!pipe.contains(Vector2D(-0.1, 0.0)))
    assert(!pipe.contains(Vector2D(100.1, 0.0)))
    assert(!pipe.contains(Vector2D(50.0, 10.1)))
    assert(!pipe.contains(Vector2D(50.0, -10.1)))
  }

  test("distance from center ignores vertical sign") {
    assert(pipe.distanceFromCenter(Vector2D(20.0, -7.5)) == 7.5)
  }

  test("clampToWalls preserves x and clamps y") {
    assert(pipe.clampToWalls(Vector2D(20.0, 15.0)) == Vector2D(20.0, 10.0))
    assert(pipe.clampToWalls(Vector2D(20.0, -15.0)) == Vector2D(20.0, -10.0))
  }
}
