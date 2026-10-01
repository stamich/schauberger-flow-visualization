package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

/** Tests continuous-flow and wall-correction boundary semantics. */
@RunWith(classOf[JUnitRunner])
final class PipeBoundaryHandlerSpec extends AnyFunSuite {
  private val handler = new PipeBoundaryHandler
  private val pipe = StraightCircularPipe(100.0, 10.0)
  private val params = SimulationParameters.Default.copy(axialVelocity = 100.0)

  test("particle before outlet remains unchanged when inside walls") {
    val particle = Particle(7L, Vector2D(50.0, 2.0), Vector2D(20.0, 1.0))
    assert(handler.handle(particle, pipe, params) == particle)
  }

  test("particle beyond outlet respawns at inlet and retains id") {
    val result = handler.handle(Particle(7L, Vector2D(101.0, 3.0), Vector2D(120.0, 2.0)), pipe, params)
    assert(result.id == 7L)
    assert(result.position.x == 0.0)
    assert(pipe.contains(result.position))
  }

  test("particle above upper wall is clamped and outward y velocity removed") {
    val result = handler.handle(Particle(1L, Vector2D(50.0, 12.0), Vector2D(20.0, 5.0)), pipe, params)
    assert(result.position.y == 10.0)
    assert(result.velocity.y == 0.0)
  }

  test("particle below lower wall is clamped and outward y velocity removed") {
    val result = handler.handle(Particle(1L, Vector2D(50.0, -12.0), Vector2D(20.0, -5.0)), pipe, params)
    assert(result.position.y == -10.0)
    assert(result.velocity.y == 0.0)
  }
}
