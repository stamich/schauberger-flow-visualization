package io.codeswarm.schauberger.physics.swirl

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TanhTransitionSpec extends AnyFunSuite {
  private val transition = TanhTransition(0.5, 0.1)

  test("weight is one half at the center") {
    assert(math.abs(transition.innerWeight(0.5) - 0.5) < 1e-12)
  }

  test("weight approaches one at the axis and zero at the wall") {
    assert(transition.innerWeight(0.0) > 0.99)
    assert(transition.innerWeight(1.0) < 0.01)
  }

  test("weight stays within the unit interval and never increases with radius") {
    val weights = (0 to 200).map(i => transition.innerWeight(i / 200.0))
    assert(weights.forall(w => w >= 0.0 && w <= 1.0))
    assert(weights.sliding(2).forall(pair => pair(1) <= pair.head + 1e-15))
  }

  test("weight is point-symmetric around the center") {
    val offset = 0.17
    assert(math.abs(transition.innerWeight(0.5 - offset) + transition.innerWeight(0.5 + offset) - 1.0) < 1e-12)
  }

  test("a narrower width gives a steeper transition") {
    val sharp = TanhTransition(0.5, 0.02)
    assert(sharp.innerWeight(0.55) < transition.innerWeight(0.55))
  }

  test("center and width are validated") {
    assertThrows[IllegalArgumentException](TanhTransition(0.0, 0.1))
    assertThrows[IllegalArgumentException](TanhTransition(1.0, 0.1))
    assertThrows[IllegalArgumentException](TanhTransition(0.5, 0.0))
  }
}
