package io.codeswarm.schauberger.model

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DefaultParametersSpec extends AnyFunSuite {
  test("milestone 0.6 keeps readable flow defaults") {
    val parameters = SimulationParameters.Default
    assert(parameters.particleCount == 750)
    assert(parameters.axial.velocity == 90.0)
    assert(parameters.swirl.angularVelocity == 0.55)
    assert(parameters.swirl.response == 2.0)
    assert(parameters.swirl.profile == SolidBodyParameters)
    assert(parameters.secondaryFlow.enabled)
    assert(parameters.secondaryFlow.strength == 18.0)
  }

  test("centripetal balance is enabled at its ideal strength by default") {
    assert(SimulationParameters.Default.radialBalance == RadialBalanceParameters(enabled = true, strength = 1.0))
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

  test("heat map defaults to the particle field with moderate smoothing") {
    val visualization = VisualizationParameters.Default
    assert(visualization.heatMapSource == HeatMapSource.Particles)
    assert(visualization.heatMapSmoothing == 0.7)
    assert(HeatMapSource.fromDisplayName("Analytic profile") == HeatMapSource.AnalyticProfile)
    assert(HeatMapSource.fromDisplayName("unknown") == HeatMapSource.Particles)
    assertThrows[IllegalArgumentException](visualization.copy(heatMapSmoothing = 1.0))
  }
}
