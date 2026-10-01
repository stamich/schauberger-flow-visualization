package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.SimulationParameters

/** Immutable context shared with all force calculations.
  *
  * @param geometry active pipe geometry
  * @param parameters current simulation parameters
  */
final case class FlowContext(geometry: PipeGeometry, parameters: SimulationParameters)
