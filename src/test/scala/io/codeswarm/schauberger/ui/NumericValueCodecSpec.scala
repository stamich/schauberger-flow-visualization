package io.codeswarm.schauberger.ui

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NumericValueCodecSpec extends AnyFunSuite {
  test("parser accepts dot and comma decimals") {
    assert(NumericValueCodec.parse("1.25").contains(1.25))
    assert(NumericValueCodec.parse("1,25").contains(1.25))
  }

  test("invalid and non-finite values are rejected") {
    assert(NumericValueCodec.parse("abc").isEmpty)
    assert(NumericValueCodec.parse("NaN").isEmpty)
    assert(NumericValueCodec.parse("Infinity").isEmpty)
  }

  test("normalization clamps and integer mode rounds") {
    assert(NumericValueCodec.normalize(15.0, 0.0, 10.0, integer = false) == 10.0)
    assert(NumericValueCodec.normalize(3.6, 0.0, 10.0, integer = true) == 4.0)
  }
}
