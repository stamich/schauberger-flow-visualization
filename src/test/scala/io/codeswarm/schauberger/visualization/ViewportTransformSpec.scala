package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.Vector2D
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ViewportTransformSpec extends AnyFunSuite {
  test("world center maps to canvas center") {
    val t = ViewportTransform(-10, 10, -10, 10, 200, 100)
    val p = t.worldToScreen(Vector2D.Zero)
    assert(math.abs(p.x - 100) < 1e-9)
    assert(math.abs(p.y - 50) < 1e-9)
  }
}
