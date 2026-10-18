package io.codeswarm.schauberger.diagnostics

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ZeroCrossingDetectorSpec extends AnyFunSuite {
  private def samples(values: Double*): Seq[RadialProfileSample] =
    values.zipWithIndex.map { case (value, index) => RadialProfileSample(index.toDouble / (values.size - 1), value) }

  test("no samples and constant-sign profiles give no crossing") {
    assert(ZeroCrossingDetector.detect(Seq.empty).isEmpty)
    assert(ZeroCrossingDetector.detect(samples(0.0, 1.0, 2.0, 1.0)).isEmpty)
    assert(ZeroCrossingDetector.detect(samples(0.0, -1.0, -2.0)).isEmpty)
  }

  test("a sign change is interpolated linearly between the samples") {
    val crossings = ZeroCrossingDetector.detect(samples(0.0, 1.0, -3.0, -4.0, -5.0))
    assert(crossings.size == 1)
    // between x = 0.25 (value 1) and x = 0.5 (value -3): 0.25 + 0.25 * 1/4
    assert(math.abs(crossings.head - 0.3125) < 1e-12)
  }

  test("a sample that is exactly zero between opposite signs is reported at that sample") {
    val crossings = ZeroCrossingDetector.detect(samples(1.0, 0.0, -1.0))
    assert(crossings == Vector(0.5))
  }

  test("a profile that only touches zero is not a crossing") {
    assert(ZeroCrossingDetector.detect(samples(1.0, 0.0, 1.0)).isEmpty)
  }

  test("several crossings come out in increasing order") {
    val crossings = ZeroCrossingDetector.detect(samples(1.0, -1.0, 1.0, -1.0))
    assert(crossings.size == 3)
    assert(crossings == crossings.sorted)
  }
}
