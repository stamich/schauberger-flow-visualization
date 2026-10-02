package io.codeswarm.schauberger.model

/** Available two-dimensional projections of the three-dimensional simulation. */
sealed trait ViewMode

/** View-mode values used by the UI and renderer. */
object ViewMode {
  case object Longitudinal extends ViewMode

  case object CrossSection extends ViewMode
}
