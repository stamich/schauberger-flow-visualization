package io.codeswarm.schauberger.physics.swirl

import io.codeswarm.schauberger.model.CounterRotatingParameters
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CounterRotatingVortexProfileSpec extends AnyFunSuite {
  private val characteristic = 100.0
  private val omega = 0.5
  private val transition = TanhTransition(0.55, 0.08)
  private val profile = CounterRotatingVortexProfile(
    RankineVortexProfile(0.4), SolidBodySwirlProfile, 1.0, 0.6, transition
  )

  private def speed(p: SwirlProfile, rho: Double): Double = p.tangentialVelocity(rho * characteristic, characteristic, omega)

  /** Finds the first radius in `(from, to)` where the sign changes, by bisection. */
  private def zeroCrossing(p: SwirlProfile, from: Double, to: Double): Double = {
    var lo = from
    var hi = to
    assert(speed(p, lo) * speed(p, hi) < 0.0, "the interval must bracket a sign change")
    var i = 0
    while (i < 80) {
      val mid = 0.5 * (lo + hi)
      if (speed(p, lo) * speed(p, mid) <= 0.0) hi = mid else lo = mid
      i += 1
    }
    0.5 * (lo + hi)
  }

  test("velocity vanishes on the axis") {
    assert(math.abs(speed(profile, 0.0)) < 1e-12)
  }

  test("inner region turns with the rotation direction and outer region against it") {
    assert(speed(profile, 0.2) > 0.0)
    assert(speed(profile, 0.95) < 0.0)
  }

  test("the profile is signed, which the simple profiles never are") {
    val values = (0 to 100).map(i => speed(profile, i / 100.0))
    assert(values.exists(_ > 0.0) && values.exists(_ < 0.0))
  }

  test("the outer layer fades out where the transition weight is one") {
    val innerOnly = speed(RankineVortexProfile(0.4), 0.05)
    assert(math.abs(speed(profile, 0.05) - innerOnly) < 0.01 * math.abs(innerOnly))
  }

  test("equal layers and strengths cross zero at the transition radius") {
    val symmetric = CounterRotatingVortexProfile(SolidBodySwirlProfile, SolidBodySwirlProfile, 1.0, 1.0, transition)
    assert(math.abs(zeroCrossing(symmetric, 0.2, 0.9) - 0.55) < 1e-9)
  }

  test("a weaker outer layer moves the zero crossing outward") {
    val weakOuter = CounterRotatingVortexProfile(SolidBodySwirlProfile, SolidBodySwirlProfile, 1.0, 0.6, transition)
    assert(zeroCrossing(weakOuter, 0.2, 0.99) > 0.55)
  }

  test("a stronger outer layer moves the zero crossing inward") {
    val strongOuter = CounterRotatingVortexProfile(SolidBodySwirlProfile, SolidBodySwirlProfile, 1.0, 2.0, transition)
    assert(zeroCrossing(strongOuter, 0.1, 0.9) < 0.55)
  }

  test("the blended velocity is continuous across the transition") {
    val samples = (0 until 1000).map(i => speed(profile, i / 1000.0))
    val largestStep = samples.sliding(2).map(pair => math.abs(pair(1) - pair.head)).max
    assert(largestStep < 0.02 * samples.map(math.abs).max)
  }

  test("velocity scales linearly with the angular velocity") {
    val double = profile.tangentialVelocity(30.0, characteristic, 2.0 * omega)
    assert(math.abs(double - 2.0 * profile.tangentialVelocity(30.0, characteristic, omega)) < 1e-12)
  }

  test("strengths must not be negative") {
    assertThrows[IllegalArgumentException](
      CounterRotatingVortexProfile(SolidBodySwirlProfile, SolidBodySwirlProfile, -1.0, 1.0, transition)
    )
  }

  test("parameters validate transition and strengths") {
    assert(CounterRotatingParameters().id == "counter-rotating")
    assertThrows[IllegalArgumentException](CounterRotatingParameters(transitionRadius = 1.0))
    assertThrows[IllegalArgumentException](CounterRotatingParameters(transitionWidth = 0.0))
    assertThrows[IllegalArgumentException](CounterRotatingParameters(outerStrength = -0.1))
  }
}
