package io.codeswarm.schauberger.diagnostics

import io.codeswarm.schauberger.math.Vector2D
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TemporalFieldAveragerSpec extends AnyFunSuite {
  private def grid(value: Double, insideCenter: Boolean = true): ScalarFieldGrid =
    ScalarFieldGrid(3, Vector.tabulate(9) { i =>
      ScalarFieldCell(Vector2D(i % 3, i / 3), value, inside = if (i == 4) insideCenter else true)
    })

  test("the first grid is returned unchanged") {
    assert(new TemporalFieldAverager().update("a", grid(4.0), 0.5) == grid(4.0))
  }

  test("later grids are blended with the configured memory") {
    val averager = new TemporalFieldAverager
    averager.update("a", grid(0.0), 0.75)
    val blended = averager.update("a", grid(8.0), 0.75)
    assert(blended.cells.forall(c => math.abs(c.value - 2.0) < 1e-12))
  }

  test("repeated identical input converges to that input") {
    val averager = new TemporalFieldAverager
    averager.update("a", grid(0.0), 0.8)
    val last = (1 to 80).map(_ => averager.update("a", grid(5.0), 0.8)).last
    assert(last.cells.forall(c => math.abs(c.value - 5.0) < 1e-6))
  }

  test("zero memory returns the current grid") {
    val averager = new TemporalFieldAverager
    averager.update("a", grid(0.0), 0.0)
    assert(averager.update("a", grid(8.0), 0.0) == grid(8.0))
  }

  test("a different key restarts the average") {
    val averager = new TemporalFieldAverager
    averager.update("velocity", grid(0.0), 0.9)
    assert(averager.update("vorticity", grid(8.0), 0.9) == grid(8.0))
  }

  test("a changed inside mask restarts the average") {
    val averager = new TemporalFieldAverager
    averager.update("a", grid(0.0), 0.9)
    assert(averager.update("a", grid(8.0, insideCenter = false), 0.9) == grid(8.0, insideCenter = false))
  }

  test("reset forgets the previous grid") {
    val averager = new TemporalFieldAverager
    averager.update("a", grid(0.0), 0.9)
    averager.reset()
    assert(averager.update("a", grid(8.0), 0.9) == grid(8.0))
  }

  test("memory must be below one") {
    assertThrows[IllegalArgumentException](new TemporalFieldAverager().update("a", grid(1.0), 1.0))
  }
}
