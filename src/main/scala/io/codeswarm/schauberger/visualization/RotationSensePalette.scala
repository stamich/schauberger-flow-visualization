package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.model.VortexRegion

/** Web colours that mark the rotation sense of particles and trails.
 *
 * Warm marks turn with the configured direction and cool marks against it,
 * matching the red and blue halves of the diverging heat-map palette.
 */
object RotationSensePalette {
  /** Particle colour when sense colouring is off. */
  val Plain = "#2f9de0"

  /** Turning with the configured rotation direction. */
  val Forward = "#ff8a4c"

  /** Turning against the configured rotation direction. */
  val Reverse = "#4cc3ff"

  /** Too slow to call a direction. */
  val Quiescent = "#9aa7b4"

  /** Colour of a region. */
  def hex(region: VortexRegion): String = region match {
    case VortexRegion.Forward => Forward
    case VortexRegion.Reverse => Reverse
    case VortexRegion.Quiescent => Quiescent
  }
}
