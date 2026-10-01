package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.model.{GeometryParameters, GeometryType}

/** Creates concrete pipe geometry from UI-friendly immutable parameters. */
final class GeometryFactory {
  /** Builds the selected geometry without leaking concrete construction into the UI. */
  def create(parameters: GeometryParameters): PipeGeometry = parameters.geometryType match {
    case GeometryType.Circular => StraightPipe(parameters.length, CircularCrossSection(parameters.width / 2.0))
    case GeometryType.Ovoid => StraightPipe(parameters.length, OvoidCrossSection(parameters.width, parameters.height, parameters.asymmetry))
    case GeometryType.TwistedOvoid => TwistedPipe(parameters.length, OvoidCrossSection(parameters.width, parameters.height, parameters.asymmetry), parameters.twistTurns)
  }
}
