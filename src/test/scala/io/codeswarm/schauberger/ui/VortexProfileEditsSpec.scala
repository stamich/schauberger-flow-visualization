package io.codeswarm.schauberger.ui

import io.codeswarm.schauberger.model._
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class VortexProfileEditsSpec extends AnyFunSuite {
  import VortexProfileEdits._

  test("every profile kind has a label and switching to it round-trips") {
    val profiles = Seq(SolidBodyParameters, RankineParameters(0.4), LambOseenParameters(1.5, 0.2), CounterRotatingParameters())
    profiles.foreach(profile => assert(kindLabel(switchKind(SolidBodyParameters, kindLabel(profile))) == kindLabel(profile)))
    assert(KindLabels.size == 4)
  }

  test("switching to Rankine keeps the core ratio of a Rankine-based composite") {
    val composite = CounterRotatingParameters(inner = RankineParameters(0.25))
    assert(switchKind(composite, RankineLabel) == RankineParameters(0.25))
  }

  test("switching to a composite builds it around the current Rankine core") {
    val switched = switchKind(RankineParameters(0.6), CounterRotatingLabel)
    assert(switched == CounterRotatingParameters(inner = RankineParameters(0.6)))
  }

  test("switching to a composite from a composite keeps it") {
    val composite = CounterRotatingParameters(outerStrength = 1.1)
    assert(switchKind(composite, CounterRotatingLabel) == composite)
  }

  test("Lamb-Oseen values survive a round trip through another kind") {
    val lamb = LambOseenParameters(2.0, 0.15)
    val away = switchKind(lamb, RankineLabel)
    assert(switchKind(away, LambOseenLabel) == LambOseenParameters(1.0, 0.30)) // the value not carried by Rankine falls back
    assert(switchKind(lamb, LambOseenLabel) == lamb)
  }

  test("unknown labels fall back to solid body") {
    assert(switchKind(RankineParameters(0.3), "nonsense") == SolidBodyParameters)
  }

  test("composite sliders edit only composites") {
    val composite = CounterRotatingParameters()
    assert(withInnerStrength(composite, 0.7).asInstanceOf[CounterRotatingParameters].innerStrength == 0.7)
    assert(withOuterStrength(composite, 0.9).asInstanceOf[CounterRotatingParameters].outerStrength == 0.9)
    assert(withTransitionRadius(composite, 0.4).asInstanceOf[CounterRotatingParameters].transitionRadius == 0.4)
    assert(withTransitionWidth(composite, 0.2).asInstanceOf[CounterRotatingParameters].transitionWidth == 0.2)
    val rankine = RankineParameters(0.35)
    assert(withInnerStrength(rankine, 0.7) == rankine)
    assert(withTransitionWidth(SolidBodyParameters, 0.2) == SolidBodyParameters)
  }

  test("the Rankine core slider edits a Rankine profile and a composite's Rankine inner layer") {
    assert(withRankineCore(RankineParameters(0.35), 0.5) == RankineParameters(0.5))
    assert(withRankineCore(CounterRotatingParameters(), 0.5).asInstanceOf[CounterRotatingParameters].inner == RankineParameters(0.5))
    val lambInner = CounterRotatingParameters(inner = LambOseenParameters(1.0, 0.3))
    assert(withRankineCore(lambInner, 0.5) == lambInner)
  }

  test("Lamb-Oseen sliders leave other profiles alone") {
    assert(withLambCore(LambOseenParameters(1.0, 0.3), 0.2) == LambOseenParameters(1.0, 0.2))
    assert(withLambCirculation(LambOseenParameters(1.0, 0.3), 2.0) == LambOseenParameters(2.0, 0.3))
    assert(withLambCore(RankineParameters(0.35), 0.2) == RankineParameters(0.35))
  }

  test("presets are unique, valid and findable by name") {
    assert(Presets.map(_.name).distinct.size == Presets.size)
    Presets.foreach(p => assert(preset(p.name).contains(p)))
    assert(preset("missing").isEmpty)
  }

  test("every preset reverses direction somewhere inside the pipe") {
    val factory = new io.codeswarm.schauberger.physics.swirl.SwirlProfileFactory
    Presets.foreach { p =>
      val profile = factory.create(p.parameters)
      val values = (1 to 100).map(i => profile.tangentialVelocity(i / 100.0 * 100.0, 100.0, 0.5))
      assert(values.exists(_ > 0.0) && values.exists(_ < 0.0), p.name)
    }
  }
}
