package io.codeswarm.schauberger.model

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class DefaultParametersSpec extends AnyFunSuite {
  test("milestone 0.3 starts with 750 particles and calmer flow") {
    assert(SimulationParameters.Default.particleCount==750)
    assert(SimulationParameters.Default.axialVelocity==90.0)
    assert(SimulationParameters.Default.angularVelocity==0.55)
    assert(SimulationParameters.Default.swirlResponse==2.0)
  }
  test("trails are short, sampled and age limited") {
    val v=VisualizationParameters.Default
    assert(v.trailLength==48); assert(v.trailDurationSeconds==2.0); assert(v.trailSampleEveryFrames==4)
  }
}
