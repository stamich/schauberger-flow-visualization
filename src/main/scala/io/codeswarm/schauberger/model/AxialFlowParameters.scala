package io.codeswarm.schauberger.model

/** Parameters controlling the target axial flow. */
final case class AxialFlowParameters(velocity: Double, response: Double) {
  require(velocity >= 0.0)
  require(response >= 0.0)
}

/** Default axial-flow settings. */
object AxialFlowParameters {
  val Default: AxialFlowParameters = AxialFlowParameters(velocity = 90.0, response = 2.2)
}
