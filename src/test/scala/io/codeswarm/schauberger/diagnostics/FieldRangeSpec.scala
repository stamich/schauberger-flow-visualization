package io.codeswarm.schauberger.diagnostics

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FieldRangeSpec extends AnyFunSuite {
  test("normalization maps range to zero and one") {
    val range = FieldRange(-2.0, 6.0)
    assert(math.abs(range.normalize(-2.0)) < 1e-12)
    assert(math.abs(range.normalize(6.0) - 1.0) < 1e-12)
    assert(math.abs(range.normalize(2.0) - 0.5) < 1e-12)
  }

  test("constant field remains finite") {
    assert(FieldRange(4.0, 4.0).normalize(4.0).isFinite)
  }

  test("symmetric range is centered on zero and contains the original") {
    val range = FieldRange(-1.0, 5.0).symmetric
    assert(range == FieldRange(-5.0, 5.0))
    assert(math.abs(range.normalize(0.0) - 0.5) < 1e-12)
    assert(FieldRange(-4.0, 2.0).symmetric == FieldRange(-4.0, 4.0))
  }

  test("only tangential velocity and vorticity are signed fields") {
    assert(FieldType.values.filter(_.signed) == Vector(FieldType.TangentialVelocity, FieldType.Vorticity))
  }

  test("signed fields get a zero-centered range and the others min-max") {
    val cells = Vector(-1.0, 3.0, 0.5, 2.0, 1.0, 0.0, -0.5, 1.5, 2.5).zipWithIndex.map { case (v, i) =>
      ScalarFieldCell(io.codeswarm.schauberger.math.Vector2D(i.toDouble, 0.0), v, inside = true)
    }
    val grid = ScalarFieldGrid(3, cells)
    assert(grid.rangeFor(FieldType.AxialVelocity) == FieldRange(-1.0, 3.0))
    assert(grid.rangeFor(FieldType.TangentialVelocity) == FieldRange(-3.0, 3.0))
  }
}
