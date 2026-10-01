package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.Vector2D
import org.scalatest.funsuite.AnyFunSuite

/** Tests projected-world to screen coordinate conversion. */
final class ViewportTransformSpec extends AnyFunSuite {
  test("world center maps to canvas center for symmetric bounds") {
    val t = ViewportTransform(-10, 10, -10, 10, 200, 100, padding = 0)
    assert(t.worldToScreen(Vector2D.Zero) == Vector2D(100, 50))
  }
}
