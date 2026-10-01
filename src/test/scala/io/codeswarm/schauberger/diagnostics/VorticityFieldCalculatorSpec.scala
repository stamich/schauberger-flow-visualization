package io.codeswarm.schauberger.diagnostics

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class VorticityFieldCalculatorSpec extends AnyFunSuite {
  test("solid body rotation produces approximately two omega vorticity") {
    val resolution = 21
    val radius = 1.0
    val step = 2.0 * radius / (resolution - 1).toDouble
    val omega = 3.0
    val cells = Vector.tabulate(resolution * resolution) { index =>
      val x = index % resolution
      val y = index / resolution
      val u = -radius + x * step
      val v = -radius + y * step
      // local velocity: vy = -omega * v, vz = omega * u
      VectorFieldCell(Vector2D(u, v), Vector3D(0.0, -omega * v, omega * u), inside = true)
    }
    val scalar = new VorticityFieldCalculator().calculate(VectorFieldGrid(resolution, cells), step)
    val center = scalar.cells((resolution / 2) * resolution + resolution / 2)
    assert(math.abs(center.value - 2.0 * omega) < 1e-8)
  }
}
