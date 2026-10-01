package io.codeswarm.schauberger.physics

import org.scalatest.funsuite.AnyFunSuite

/** Tests the linear solid-body swirl profile. */
final class SolidBodySwirlProfileSpec extends AnyFunSuite {
  private val profile = new SolidBodySwirlProfile

  test("tangential velocity is zero on the axis") {
    assert(profile.tangentialVelocity(0.0, 10.0, 2.0) == 0.0)
  }

  test("tangential velocity grows linearly with radius") {
    assert(profile.tangentialVelocity(1.0, 10.0, 2.0) == 2.0)
    assert(profile.tangentialVelocity(3.0, 10.0, 2.0) == 6.0)
  }
}
