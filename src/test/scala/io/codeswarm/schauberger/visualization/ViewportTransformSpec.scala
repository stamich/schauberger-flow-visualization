package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.Vector2D
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

/** Unit tests for world-to-screen coordinate conversion. */
@RunWith(classOf[JUnitRunner])
final class ViewportTransformSpec extends AnyFunSuite {
  private val transform = ViewportTransform(
    worldWidth = 1000.0,
    worldHalfHeight = 100.0,
    canvasWidth = 1100.0,
    canvasHeight = 300.0,
    horizontalPadding = 50.0,
    verticalPadding = 50.0
  )

  test("world origin maps to left padding and canvas vertical center") {
    assert(transform.worldToScreen(Vector2D(0.0, 0.0)) == Vector2D(50.0, 150.0))
  }

  test("world right edge maps to right padding") {
    assert(transform.worldToScreen(Vector2D(1000.0, 0.0)) == Vector2D(1050.0, 150.0))
  }

  test("positive world y maps upward on screen") {
    val upper = transform.worldToScreen(Vector2D(0.0, 100.0))
    val lower = transform.worldToScreen(Vector2D(0.0, -100.0))
    assert(upper.y == 50.0)
    assert(lower.y == 250.0)
  }
}
