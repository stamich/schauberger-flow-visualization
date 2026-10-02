package io.codeswarm.schauberger.diagnostics

/** Summary produced from sampled tangential vortex velocities. */
final case class VortexProfileDiagnostics(
                                           samples: Vector[RadialProfileSample],
                                           peakVelocity: Double,
                                           peakRadius: Double
                                         )
