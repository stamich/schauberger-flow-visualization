package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LocalFrameSpec extends AnyFunSuite {
  test("cross-section vector conversion round-trips") {
    val angle = 0.73
    val rotation = Rotation2D.fromAngle(angle)
    val frame = LocalFrame(
      origin = Vector3D(10.0, 0.0, 0.0),
      tangent = Vector3D.UnitX,
      normal = Vector3D(0.0, rotation.cosine, rotation.sine),
      binormal = Vector3D(0.0, -rotation.sine, rotation.cosine),
      rotation = rotation
    )
    val local = Vector2D(3.0, -5.0)
    val recovered = frame.worldVectorToCrossSection(frame.crossSectionVectorToWorld(local))
    assert((recovered - local).magnitude < 1e-9)
  }

  test("local point conversion round-trips through frame origin") {
    val frame = LocalFrame(Vector3D(12.0, 0.0, 0.0), Vector3D.UnitX, Vector3D.UnitY, Vector3D.UnitZ, Rotation2D.Identity)
    val local = Vector2D(2.0, -3.0)
    assert((frame.worldPointToLocal(frame.localPointToWorld(local)) - local).magnitude < 1e-9)
  }
}
