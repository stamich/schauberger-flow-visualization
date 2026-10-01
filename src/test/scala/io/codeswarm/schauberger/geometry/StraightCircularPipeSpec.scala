package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector3D
import org.scalatest.funsuite.AnyFunSuite

/** Tests three-dimensional circular-pipe geometry. */
final class StraightCircularPipeSpec extends AnyFunSuite {
  private val pipe = StraightCircularPipe(100.0, 10.0)

  test("contains points inside axial and circular bounds") {
    assert(pipe.contains(Vector3D(50, 6, 8)))
    assert(!pipe.contains(Vector3D(50, 8, 8)))
    assert(!pipe.contains(Vector3D(101, 0, 0)))
  }

  test("radial distance uses y and z coordinates") {
    assert(math.abs(pipe.radialDistance(Vector3D(20, 6, 8)) - 10.0) < 1e-12)
  }

  test("center line and tangent are aligned with x") {
    assert(pipe.centerLinePosition(12.0) == Vector3D(12.0, 0.0, 0.0))
    assert(pipe.tangentAt(Vector3D.Zero) == Vector3D.UnitX)
  }

  test("clamp projects an outside point to the circular wall") {
    val p = pipe.clampToWalls(Vector3D(20, 12, 0), epsilon = 0.1)
    assert(math.abs(pipe.radialDistance(p) - 9.9) < 1e-10)
  }
}
