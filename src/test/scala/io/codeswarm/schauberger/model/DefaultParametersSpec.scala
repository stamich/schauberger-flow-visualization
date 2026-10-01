package io.codeswarm.schauberger.model

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DefaultParametersSpec extends AnyFunSuite {
  test("milestone 0.5 keeps readable flow defaults") {
    val parameters = SimulationParameters.Default
    assert(parameters.particleCount == 750)
    assert(parameters.axial.velocity == 90.0)
    assert(parameters.swirl.angularVelocity == 0.55)
    assert(parameters.swirl.response == 2.0)
    assert(parameters.secondaryFlow.enabled)
    assert(parameters.secondaryFlow.strength == 18.0)
  }

  test("trails remain short, sampled and age limited") {
    val visualization = VisualizationParameters.Default
    assert(visualization.trailLength == 48)
    assert(visualization.trailDurationSeconds == 2.0)
    assert(visualization.trailSampleEveryFrames == 4)
    assert(!visualization.showSecondaryVectors)
    assert(visualization.showHeatMap)
    assert(visualization.heatMapResolution == 30)
    assert(visualization.vectorFieldResolution == 15)
  }
}
