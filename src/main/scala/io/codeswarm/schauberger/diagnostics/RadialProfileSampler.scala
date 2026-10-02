package io.codeswarm.schauberger.diagnostics

import io.codeswarm.schauberger.model.SwirlParameters
import io.codeswarm.schauberger.physics.swirl.SwirlProfileFactory

/** Samples the selected analytic vortex profile without running particles. */
final class RadialProfileSampler(profileFactory: SwirlProfileFactory) {
  /** Samples radii from zero through the characteristic pipe radius. */
  def sample(
      parameters: SwirlParameters,
      characteristicRadius: Double,
      sampleCount: Int = 100
  ): VortexProfileDiagnostics = {
    require(characteristicRadius > 0.0)
    require(sampleCount >= 2)
    val profile = profileFactory.create(parameters.profile)
    val samples = Vector.tabulate(sampleCount) { index =>
      val normalized = index.toDouble / (sampleCount - 1).toDouble
      val radius = normalized * characteristicRadius
      RadialProfileSample(
        normalized,
        profile.tangentialVelocity(radius, characteristicRadius, parameters.angularVelocity)
      )
    }
    val peak = samples.maxBy(_.tangentialVelocity)
    VortexProfileDiagnostics(samples, peak.tangentialVelocity, peak.normalizedRadius)
  }
}
