package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.physics.swirl.SolidBodySwirlProfile
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SolidBodySwirlProfileSpec extends AnyFunSuite {
  test("tangential target scales linearly with radius") {
    assert(SolidBodySwirlProfile.tangentialVelocity(2.0, 10.0, 0.5) == 1.0)
    assert(SolidBodySwirlProfile.tangentialVelocity(4.0, 10.0, 0.5) == 2.0)
  }
}
