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

  /** Axial vorticity (1/r) d(r v)/dr by central differences. */
  private def vorticity(profile: SwirlProfile, radius: Double, characteristic: Double, omega: Double): Double = {
    val h = 1e-4 * characteristic
    def circulationDensity(r: Double): Double = r * profile.tangentialVelocity(r, characteristic, omega)
    (circulationDensity(radius + h) - circulationDensity(radius - h)) / (2.0 * h) / radius
  }

  test("vorticity is twice the angular velocity in the core and zero outside it") {
    val profile = RankineVortexProfile(0.4)
    val characteristic = 100.0
    val omega = 0.5
    assert(math.abs(vorticity(profile, 20.0, characteristic, omega) - 2.0 * omega) < 1e-6)
    assert(math.abs(vorticity(profile, 70.0, characteristic, omega)) < 1e-6)
  }

  test("circulation outside the core is constant") {
    val profile = RankineVortexProfile(0.3)
    val characteristic = 50.0
    val gamma = (r: Double) => 2.0 * math.Pi * r * profile.tangentialVelocity(r, characteristic, 0.7)
    assert(math.abs(gamma(20.0) - gamma(45.0)) < 1e-9)
  }
}
