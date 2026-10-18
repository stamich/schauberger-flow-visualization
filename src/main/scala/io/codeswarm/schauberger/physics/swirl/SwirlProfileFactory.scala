package io.codeswarm.schauberger.physics.swirl

import io.codeswarm.schauberger.model.{CounterRotatingParameters, LambOseenParameters, RankineParameters, SimpleVortexProfileParameters, SolidBodyParameters, VortexProfileParameters}
import java.util.concurrent.atomic.AtomicReference

/** Centralizes mapping from immutable profile parameters to profile strategies.
 *
 * The factory remembers only the most recently requested parameters. All
 * particles of one simulation step share a single parameter object, so the hot
 * path is one reference comparison, while dragging a UI slider can never grow
 * the cache: older entries are simply replaced.
 */
final class SwirlProfileFactory {
  /** Last requested parameters together with the strategy built for them. */
  private final case class Entry(parameters: VortexProfileParameters, profile: SwirlProfile)

  private val last = new AtomicReference[Entry](Entry(SolidBodyParameters, SolidBodySwirlProfile))

  /** Returns the strategy for the supplied profile parameters, rebuilding it only when they changed. */
  def create(parameters: VortexProfileParameters): SwirlProfile = {
    val entry = last.get()
    if ((entry.parameters eq parameters) || entry.parameters == parameters) entry.profile
    else {
      val profile = build(parameters)
      last.set(Entry(parameters, profile))
      profile
    }
  }

  /** Builds one concrete profile. */
  private def build(parameters: VortexProfileParameters): SwirlProfile = parameters match {
    case simple: SimpleVortexProfileParameters => buildSimple(simple)
    case composite: CounterRotatingParameters =>
      CounterRotatingVortexProfile(
        buildSimple(composite.inner),
        buildSimple(composite.outer),
        composite.innerStrength,
        composite.outerStrength,
        TanhTransition(composite.transitionRadius, composite.transitionWidth)
      )
  }

  /** Builds a single-vortex profile. */
  private def buildSimple(parameters: SimpleVortexProfileParameters): SwirlProfile = parameters match {
    case SolidBodyParameters => SolidBodySwirlProfile
    case RankineParameters(coreRadiusRatio) => RankineVortexProfile(coreRadiusRatio)
    case LambOseenParameters(circulation, coreRadiusRatio) => LambOseenVortexProfile(circulation, coreRadiusRatio)
  }
}
