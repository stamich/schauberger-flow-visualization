package io.codeswarm.schauberger.physics.swirl

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RankineVortexProfileSpec extends AnyFunSuite {
  test("profile is linear inside core and inverse-radius outside") {
    val profile = RankineVortexProfile(0.4)
    val radius = 10.0
    assert(math.abs(profile.tangentialVelocity(2.0, radius, 0.5) - 1.0) < 1e-12)
    val outside = profile.tangentialVelocity(8.0, radius, 0.5)
    assert(math.abs(outside - 1.0) < 1e-12)
  }

  test("profile is continuous at the core boundary") {
    val profile = RankineVortexProfile(0.35)
    val characteristic = 100.0
    val core = characteristic * 0.35
    val left = profile.tangentialVelocity(core * (1.0 - 1e-9), characteristic, 0.55)
    val right = profile.tangentialVelocity(core * (1.0 + 1e-9), characteristic, 0.55)
    assert(math.abs(left - right) < 1e-5)
  }
}
