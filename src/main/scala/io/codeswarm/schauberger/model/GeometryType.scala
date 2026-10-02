package io.codeswarm.schauberger.model

/** Geometry choices exposed in milestone 0.6. */
sealed trait GeometryType

/** Supported pipe geometry identifiers. */
object GeometryType {
  case object Circular extends GeometryType

  case object Ovoid extends GeometryType

  case object TwistedOvoid extends GeometryType
}
