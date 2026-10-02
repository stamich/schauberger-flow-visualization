package io.codeswarm.schauberger.physics.swirl

import io.codeswarm.schauberger.model.{LambOseenParameters, RankineParameters, SolidBodyParameters, VortexProfileParameters}
import scala.collection.concurrent.TrieMap

/** Centralizes mapping from immutable profile parameters to profile strategies.
  *
  * A small concurrent cache avoids allocating one profile object per particle
  * update while keeping all type selection in one place.
  */
final class SwirlProfileFactory {
  private val cache = TrieMap.empty[VortexProfileParameters, SwirlProfile]

  /** Returns a cached strategy for the supplied profile parameters. */
  def create(parameters: VortexProfileParameters): SwirlProfile =
    cache.getOrElseUpdate(parameters, build(parameters))

  /** Builds one concrete profile. */
  private def build(parameters: VortexProfileParameters): SwirlProfile = parameters match {
    case SolidBodyParameters => SolidBodySwirlProfile
    case RankineParameters(coreRadiusRatio) => RankineVortexProfile(coreRadiusRatio)
    case LambOseenParameters(circulation, coreRadiusRatio) => LambOseenVortexProfile(circulation, coreRadiusRatio)
  }
}
