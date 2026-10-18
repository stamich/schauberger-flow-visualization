package io.codeswarm.schauberger.visualization

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DivergingPaletteSpec extends AnyFunSuite {
  test("the middle of the range is neutral") {
    assert(DivergingPalette.color(0.5).saturation == 0.0)
  }

  test("the negative half is blue and the positive half is red") {
    assert(DivergingPalette.color(0.1).hue == DivergingPalette.NegativeHue)
    assert(DivergingPalette.color(0.9).hue == DivergingPalette.PositiveHue)
  }

  test("saturation grows toward both ends and is symmetric") {
    assert(DivergingPalette.color(0.25).saturation < DivergingPalette.color(0.0).saturation)
    assert(math.abs(DivergingPalette.color(0.2).saturation - DivergingPalette.color(0.8).saturation) < 1e-12)
  }

  test("values outside the unit interval are clamped") {
    assert(DivergingPalette.color(-3.0) == DivergingPalette.color(0.0))
    assert(DivergingPalette.color(7.0) == DivergingPalette.color(1.0))
  }
}
