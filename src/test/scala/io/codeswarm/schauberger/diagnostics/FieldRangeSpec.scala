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
}
