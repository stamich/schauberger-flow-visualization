package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.SimulationParameters

/** Read-only environment supplied to every force calculation.
  *
  * @param geometry current pipe geometry
  * @param parameters current user/numerical parameters
  */
final case class FlowContext(geometry: PipeGeometry, parameters: SimulationParameters)
