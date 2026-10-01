package io.codeswarm.schauberger.model

/** Aggregate diagnostics calculated from the current particle state.
  *
  * @param meanAxialVelocity mean x component of particle velocity
  * @param meanTangentialVelocity mean signed tangential velocity around the pipe axis
  * @param meanAngularVelocity mean signed angular velocity v-theta/r away from the axis
  * @param vorticityProxy twice the mean angular velocity for the solid-body model
  */
final case class FlowMetrics(
    meanAxialVelocity: Double,
    meanTangentialVelocity: Double,
    meanAngularVelocity: Double,
    vorticityProxy: Double
)

/** Zero-valued diagnostics used for empty states. */
object FlowMetrics {
  /** Metrics representing no measured flow. */
  val Zero: FlowMetrics = FlowMetrics(0.0, 0.0, 0.0, 0.0)
}
