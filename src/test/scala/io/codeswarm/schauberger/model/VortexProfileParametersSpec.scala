package io.codeswarm.schauberger.model

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class VortexProfileParametersSpec extends AnyFunSuite {
  test("the single-vortex parameter types are usable as composite layers") {
    val layers: Seq[SimpleVortexProfileParameters] =
      Seq(SolidBodyParameters, RankineParameters(0.3), LambOseenParameters(1.0, 0.3))
    assert(layers.map(_.id) == Seq("solid-body", "rankine", "lamb-oseen"))
  }

  test("every simple parameter set is also a general vortex profile parameter set") {
    val general: VortexProfileParameters = RankineParameters(0.5)
    assert(general.isInstanceOf[SimpleVortexProfileParameters])
  }
}
