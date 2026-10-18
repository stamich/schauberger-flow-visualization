package io.codeswarm.schauberger.ui

import io.codeswarm.schauberger.model._

/** A named counter-rotating configuration offered by the profile pane. */
final case class VortexPreset(name: String, parameters: CounterRotatingParameters)

/** Pure editing rules behind the vortex-profile pane.
 *
 * Keeping them free of ScalaFX makes the switching and slider behaviour
 * testable: changing the kind keeps shared values such as the core ratio, and a
 * slider never alters a profile it does not belong to.
 */
object VortexProfileEdits {
  val SolidBodyLabel = "Solid body"
  val RankineLabel = "Rankine"
  val LambOseenLabel = "Lamb-Oseen"
  val CounterRotatingLabel = "Counter-rotating"

  /** Profile kinds in the order the selector lists them. */
  val KindLabels: Vector[String] = Vector(SolidBodyLabel, RankineLabel, LambOseenLabel, CounterRotatingLabel)

  /** Ready-made counter-rotating flows, from a gentle reversal to a strong shear layer. */
  val Presets: Vector[VortexPreset] = Vector(
    VortexPreset("Core and shell", CounterRotatingParameters()),
    VortexPreset("Strong reverse shell", CounterRotatingParameters(innerStrength = 0.8, outerStrength = 1.2, transitionRadius = 0.5, transitionWidth = 0.08)),
    VortexPreset("Gentle reversal", CounterRotatingParameters(innerStrength = 1.0, outerStrength = 0.3, transitionRadius = 0.65, transitionWidth = 0.12)),
    VortexPreset("Thin shear layer", CounterRotatingParameters(innerStrength = 1.0, outerStrength = 1.0, transitionRadius = 0.5, transitionWidth = 0.03))
  )

  /** Looks up a preset by name. */
  def preset(name: String): Option[VortexPreset] = Presets.find(_.name == name)

  /** Selector label of a profile. */
  def kindLabel(profile: VortexProfileParameters): String = profile match {
    case SolidBodyParameters => SolidBodyLabel
    case _: RankineParameters => RankineLabel
    case _: LambOseenParameters => LambOseenLabel
    case _: CounterRotatingParameters => CounterRotatingLabel
  }

  /** Rankine core ratio of the profile, or the default when it has none. A composite reports its inner Rankine core. */
  def rankineCore(profile: VortexProfileParameters): Double = profile match {
    case RankineParameters(core) => core
    case CounterRotatingParameters(RankineParameters(core), _, _, _, _, _) => core
    case _ => 0.35
  }

  /** Lamb-Oseen core ratio of the profile, or the default. */
  def lambCore(profile: VortexProfileParameters): Double = profile match {
    case LambOseenParameters(_, core) => core
    case _ => 0.30
  }

  /** Lamb-Oseen circulation of the profile, or the default. */
  def lambCirculation(profile: VortexProfileParameters): Double = profile match {
    case LambOseenParameters(circulation, _) => circulation
    case _ => 1.0
  }

  /** The composite currently configured, or the default composite built around the current Rankine core. */
  def counterRotating(profile: VortexProfileParameters): CounterRotatingParameters = profile match {
    case composite: CounterRotatingParameters => composite
    case other => CounterRotatingParameters(inner = RankineParameters(rankineCore(other)))
  }

  /** Switches to another kind, carrying over the core ratios and circulation the target kind shares. */
  def switchKind(current: VortexProfileParameters, label: String): VortexProfileParameters = label match {
    case RankineLabel => RankineParameters(rankineCore(current))
    case LambOseenLabel => LambOseenParameters(lambCirculation(current), lambCore(current))
    case CounterRotatingLabel => counterRotating(current)
    case _ => SolidBodyParameters
  }

  /** Sets the Rankine core ratio of a Rankine profile or of a composite's inner Rankine layer; other profiles are unchanged. */
  def withRankineCore(profile: VortexProfileParameters, core: Double): VortexProfileParameters = profile match {
    case _: RankineParameters => RankineParameters(core)
    case composite: CounterRotatingParameters if composite.inner.isInstanceOf[RankineParameters] =>
      composite.copy(inner = RankineParameters(core))
    case other => other
  }

  /** Sets the Lamb-Oseen core ratio; other profiles are unchanged. */
  def withLambCore(profile: VortexProfileParameters, core: Double): VortexProfileParameters = profile match {
    case LambOseenParameters(circulation, _) => LambOseenParameters(circulation, core)
    case other => other
  }

  /** Sets the Lamb-Oseen circulation; other profiles are unchanged. */
  def withLambCirculation(profile: VortexProfileParameters, circulation: Double): VortexProfileParameters = profile match {
    case LambOseenParameters(_, core) => LambOseenParameters(circulation, core)
    case other => other
  }

  /** Sets the inner-layer strength of a composite; other profiles are unchanged. */
  def withInnerStrength(profile: VortexProfileParameters, strength: Double): VortexProfileParameters = profile match {
    case composite: CounterRotatingParameters => composite.copy(innerStrength = strength)
    case other => other
  }

  /** Sets the outer-layer strength of a composite; other profiles are unchanged. */
  def withOuterStrength(profile: VortexProfileParameters, strength: Double): VortexProfileParameters = profile match {
    case composite: CounterRotatingParameters => composite.copy(outerStrength = strength)
    case other => other
  }

  /** Sets the transition radius of a composite; other profiles are unchanged. */
  def withTransitionRadius(profile: VortexProfileParameters, radius: Double): VortexProfileParameters = profile match {
    case composite: CounterRotatingParameters => composite.copy(transitionRadius = radius)
    case other => other
  }

  /** Sets the transition width of a composite; other profiles are unchanged. */
  def withTransitionWidth(profile: VortexProfileParameters, width: Double): VortexProfileParameters = profile match {
    case composite: CounterRotatingParameters => composite.copy(transitionWidth = width)
    case other => other
  }
}
