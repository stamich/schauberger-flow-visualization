package io.codeswarm.schauberger.model

/** Aggregate diagnostics displayed by the UI. */
final case class FlowMetrics(
    meanAxialVelocity: Double,
    meanTangentialVelocity: Double,
    meanAngularVelocity: Double,
    vorticityProxy: Double,
    meanNormalizedRadialPosition: Double
)
/** Zero-valued metrics for empty states. */
object FlowMetrics { val Zero: FlowMetrics = FlowMetrics(0.0, 0.0, 0.0, 0.0, 0.0) }
