package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.{OvoidCrossSection, TwistedPipe}
import io.codeswarm.schauberger.model.SecondaryFlowParameters
import io.codeswarm.schauberger.physics.secondary.TwinVortexSecondaryFlow
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SecondaryFlowFieldSamplerSpec extends AnyFunSuite {
  test("sampler returns only finite samples inside the cross-section") {
    val geometry = TwistedPipe(100.0, OvoidCrossSection(20.0, 30.0, 0.1), 1.0)
    val samples = new SecondaryFlowFieldSampler(new TwinVortexSecondaryFlow)
      .sample(geometry, 50.0, SecondaryFlowParameters.Default, 15)
    assert(samples.nonEmpty)
    assert(samples.forall(sample => geometry.crossSection.contains(sample.position)))
    assert(samples.forall(sample => Seq(sample.velocity.x, sample.velocity.y).forall(value => !value.isNaN && !value.isInfinity)))
  }
}
