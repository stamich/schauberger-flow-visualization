package io.codeswarm.schauberger.model

/** Immutable physical and numerical parameters for milestone 0.4.
  *
  * Flow-domain settings are grouped so the simulation can grow without turning
  * this type into a flat list of unrelated numeric values.
  */
final case class SimulationParameters(
    particleCount: Int,
    axial: AxialFlowParameters,
    swirl: SwirlParameters,
    secondaryFlow: SecondaryFlowParameters,
    wall: WallParameters,
    maxVelocity: Double,
    fixedTimeStep: Double
) {
  require(particleCount > 0)
  require(maxVelocity > 0.0)
  require(fixedTimeStep > 0.0)
}

/** Default physical settings. */
object SimulationParameters {
  val Default: SimulationParameters = SimulationParameters(
    particleCount = 750,
    axial = AxialFlowParameters.Default,
    swirl = SwirlParameters.Default,
    secondaryFlow = SecondaryFlowParameters.Default,
    wall = WallParameters.Default,
    maxVelocity = 210.0,
    fixedTimeStep = 1.0 / 120.0
  )
}
