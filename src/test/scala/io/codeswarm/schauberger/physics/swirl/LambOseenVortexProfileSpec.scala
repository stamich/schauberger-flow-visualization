package io.codeswarm.schauberger.physics.swirl

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LambOseenVortexProfileSpec extends AnyFunSuite {
  test("center is finite and evaluates to zero") {
    val profile = LambOseenVortexProfile(1.0, 0.3)
    assert(profile.tangentialVelocity(0.0, 100.0, 0.55) == 0.0)
  }

  test("all sampled velocities are finite and non-negative") {
    val profile = LambOseenVortexProfile(1.0, 0.3)
    (0 to 1000).foreach { i =>
      val radius = i / 10.0
      val velocity = profile.tangentialVelocity(radius, 100.0, 0.55)
      assert(!velocity.isNaN && !velocity.isInfinity)
      assert(velocity >= 0.0)
    }
  }

  test("profile eventually decays after its peak") {
    val profile = LambOseenVortexProfile(1.0, 0.2)
    val nearCore = profile.tangentialVelocity(20.0, 100.0, 0.55)
    val far = profile.tangentialVelocity(100.0, 100.0, 0.55)
    assert(nearCore > far)
  }
}
