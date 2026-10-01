package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LocalFrameSpec extends AnyFunSuite {
  test("cross-section vector conversion round-trips") {
    val angle = 0.73
    val frame = LocalFrame(
      Vector3D.UnitX,
      Vector3D(0.0, math.cos(angle), math.sin(angle)),
      Vector3D(0.0, -math.sin(angle), math.cos(angle)),
      angle
    )
    val local = Vector2D(3.0, -5.0)
    val recovered = frame.worldVectorToCrossSection(frame.crossSectionVectorToWorld(local))
    assert((recovered - local).magnitude < 1e-9)
  }
}
