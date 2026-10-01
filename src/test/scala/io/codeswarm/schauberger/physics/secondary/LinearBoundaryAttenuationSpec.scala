package io.codeswarm.schauberger.physics.secondary

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LinearBoundaryAttenuationSpec extends AnyFunSuite {
  test("attenuation is zero at wall and one outside fade zone") {
    assert(LinearBoundaryAttenuation.factor(0.0, 10.0) == 0.0)
    assert(math.abs(LinearBoundaryAttenuation.factor(5.0, 10.0) - 0.5) < 1e-9)
    assert(LinearBoundaryAttenuation.factor(10.0, 10.0) == 1.0)
    assert(LinearBoundaryAttenuation.factor(20.0, 10.0) == 1.0)
  }
}
