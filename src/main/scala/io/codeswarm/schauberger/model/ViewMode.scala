package io.codeswarm.schauberger.model

/** Available two-dimensional projections of the three-dimensional simulation. */
sealed trait ViewMode

/** Supported visualization modes. */
object ViewMode {
  /** Side view using x and y coordinates. */
  case object Longitudinal extends ViewMode

  /** Cross-section view using y and z coordinates. */
  case object CrossSection extends ViewMode
}
