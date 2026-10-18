package io.codeswarm.schauberger.diagnostics

import io.codeswarm.schauberger.geometry.GeometryFactory
import io.codeswarm.schauberger.model.{CounterRotatingParameters, GeometryParameters, RotationDirection, SimulationParameters}
import io.codeswarm.schauberger.physics.swirl.SwirlProfileFactory
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AnalyticProfileFieldSamplerSpec extends AnyFunSuite {
  private val geometry = new GeometryFactory().create(GeometryParameters.Default)
  private val factory = new SwirlProfileFactory
  private val sampler = new AnalyticProfileFieldSampler(factory)
  private val calculator = new ScalarFieldCalculator
  private val resolution = 41
  private val swirl = SimulationParameters.Default.swirl

  private def tangential(parameters: io.codeswarm.schauberger.model.SwirlParameters): ScalarFieldGrid =
    calculator.calculate(FieldType.TangentialVelocity, sampler.sample(geometry, parameters, resolution), geometry)

  test("grid has the requested resolution and masks cells outside the pipe") {
    val grid = sampler.sample(geometry, swirl, resolution)
    assert(grid.resolution == resolution)
    assert(grid.cells.exists(!_.inside) && grid.cells.exists(_.inside))
    assert(grid.cells.filterNot(_.inside).forall(_.velocity.magnitude == 0.0))
  }

  test("tangential field reproduces the profile at every inside cell") {
    val profile = factory.create(swirl.profile)
    tangential(swirl).cells.filter(c => c.inside && c.position.magnitude > 1e-9).foreach { cell =>
      val expected = profile.tangentialVelocity(cell.position.magnitude, geometry.characteristicRadius, swirl.angularVelocity)
      assert(math.abs(cell.value - expected) < 1e-9)
    }
  }

  test("reversing the rotation direction flips the sign of the field") {
    val ccw = tangential(swirl.copy(rotationDirection = RotationDirection.CounterClockwise))
    val cw = tangential(swirl.copy(rotationDirection = RotationDirection.Clockwise))
    ccw.cells.zip(cw.cells).foreach { case (a, b) => assert(math.abs(a.value + b.value) < 1e-9) }
  }

  test("a counter-rotating profile produces both signs, which a heat map must show") {
    val field = tangential(swirl.copy(profile = CounterRotatingParameters()))
    val inside = field.cells.filter(_.inside)
    assert(inside.exists(_.value > 1e-6) && inside.exists(_.value < -1e-6))
    assert(field.rangeFor(FieldType.TangentialVelocity).min < 0.0)
  }

  test("the axial component of the analytic field is zero") {
    val axial = calculator.calculate(FieldType.AxialVelocity, sampler.sample(geometry, swirl, resolution), geometry)
    assert(axial.cells.forall(_.value == 0.0))
  }

  test("resolution below three is rejected") {
    assertThrows[IllegalArgumentException](sampler.sample(geometry, swirl, 2))
  }
}
