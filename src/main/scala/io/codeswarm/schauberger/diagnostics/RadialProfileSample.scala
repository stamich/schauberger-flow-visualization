package io.codeswarm.schauberger.diagnostics

/** One normalized-radius sample of an analytic vortex profile. */
final case class RadialProfileSample(
    normalizedRadius: Double,
    tangentialVelocity: Double
)
