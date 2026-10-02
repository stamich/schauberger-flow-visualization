package io.codeswarm.schauberger.physics.swirl

import io.codeswarm.schauberger.model.{LambOseenParameters, RankineParameters, SolidBodyParameters}
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
}
