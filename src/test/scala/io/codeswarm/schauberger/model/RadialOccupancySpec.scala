package io.codeswarm.schauberger.model

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RadialOccupancySpec extends AnyFunSuite {
  test("counts are normalized into fractions that sum to one") {
    val occupancy = RadialOccupancy.fromCounts(1L, 3L, 6L)
    assert(math.abs(occupancy.inner - 0.1) < 1e-12)
    assert(math.abs(occupancy.middle - 0.3) < 1e-12)
    assert(math.abs(occupancy.outer - 0.6) < 1e-12)
    assert(math.abs(occupancy.inner + occupancy.middle + occupancy.outer - 1.0) < 1e-12)
  }

  test("an empty population has empty occupancy") {
    assert(RadialOccupancy.fromCounts(0L, 0L, 0L) == RadialOccupancy.Empty)
  }

  test("thirds are split at one third and two thirds of the normalized radius") {
    assert(RadialOccupancy.thirdOf(0.0) == 0)
    assert(RadialOccupancy.thirdOf(0.33) == 0)
    assert(RadialOccupancy.thirdOf(1.0 / 3.0) == 1)
    assert(RadialOccupancy.thirdOf(0.66) == 1)
    assert(RadialOccupancy.thirdOf(2.0 / 3.0) == 2)
    assert(RadialOccupancy.thirdOf(1.0) == 2)
  }
}
