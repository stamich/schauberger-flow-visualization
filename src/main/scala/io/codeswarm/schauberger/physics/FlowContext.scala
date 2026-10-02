package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.SimulationParameters

/** Immutable context shared by all force calculations. */
final case class FlowContext(geometry: PipeGeometry, parameters: SimulationParameters)
