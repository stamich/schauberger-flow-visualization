package io.codeswarm.schauberger.physics.swirl

import io.codeswarm.schauberger.model.{CounterRotatingParameters, LambOseenParameters, RankineParameters, SolidBodyParameters}
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SwirlProfileFactorySpec extends AnyFunSuite {
  test("factory returns strategies for all supported profile parameter ADTs") {
    val factory = new SwirlProfileFactory
    assert(factory.create(SolidBodyParameters) eq SolidBodySwirlProfile)
    assert(factory.create(RankineParameters(0.35)).isInstanceOf[RankineVortexProfile])
    assert(factory.create(LambOseenParameters(1.0, 0.3)).isInstanceOf[LambOseenVortexProfile])
  }

  test("factory caches profile instances for equal parameter values") {
    val factory = new SwirlProfileFactory
    val parameters = RankineParameters(0.35)
    assert(factory.create(parameters) eq factory.create(parameters))
  }

  test("equal but distinct parameter instances share one cached profile") {
    val factory = new SwirlProfileFactory
    assert(factory.create(RankineParameters(0.35)) eq factory.create(RankineParameters(0.35)))
  }

  test("dragging a slider never returns a stale profile") {
    val factory = new SwirlProfileFactory
    val ratios = (1 to 2000).map(i => 0.05 + 0.95 * i / 2000.0)
    ratios.foreach { ratio =>
      factory.create(RankineParameters(ratio)) match {
        case RankineVortexProfile(core) => assert(core == ratio)
        case other => fail(s"unexpected profile $other")
      }
    }
  }

  test("alternating between two parameter sets keeps returning the matching profile") {
    val factory = new SwirlProfileFactory
    val rankine = RankineParameters(0.4)
    val lamb = LambOseenParameters(1.5, 0.25)
    (1 to 5).foreach { _ =>
      assert(factory.create(rankine) == RankineVortexProfile(0.4))
      assert(factory.create(lamb) == LambOseenVortexProfile(1.5, 0.25))
      assert(factory.create(SolidBodyParameters) eq SolidBodySwirlProfile)
    }
  }

  test("counter-rotating parameters build a composite from the configured layers") {
    val parameters = CounterRotatingParameters(
      inner = RankineParameters(0.3), outer = LambOseenParameters(1.0, 0.5),
      innerStrength = 0.9, outerStrength = 0.4, transitionRadius = 0.6, transitionWidth = 0.1
    )
    new SwirlProfileFactory().create(parameters) match {
      case CounterRotatingVortexProfile(inner, outer, innerStrength, outerStrength, transition) =>
        assert(inner == RankineVortexProfile(0.3))
        assert(outer == LambOseenVortexProfile(1.0, 0.5))
        assert(innerStrength == 0.9 && outerStrength == 0.4)
        assert(transition == TanhTransition(0.6, 0.1))
      case other => fail(s"unexpected profile $other")
    }
  }

  test("a composite is cached like any other profile") {
    val factory = new SwirlProfileFactory
    assert(factory.create(CounterRotatingParameters()) eq factory.create(CounterRotatingParameters()))
  }
}
