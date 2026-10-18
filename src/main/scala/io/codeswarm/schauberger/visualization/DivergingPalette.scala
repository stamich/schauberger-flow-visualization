package io.codeswarm.schauberger.visualization

/** Hue, saturation and brightness of a palette entry; hue in degrees, the others in `[0, 1]`. */
final case class HsbColor(hue: Double, saturation: Double, brightness: Double)

/** Diverging palette for signed scalar fields.
 *
 * The middle of the range (value one half, which is zero for a range centered
 * on zero) is neutral. Moving toward 0 gives increasingly saturated blue and
 * toward 1 increasingly saturated red, so the rotation sense can be read at a
 * glance and the zero crossing of a counter-rotating vortex shows as the
 * neutral band between the two colours.
 */
object DivergingPalette {
  /** Hue used for the negative half. */
  val NegativeHue: Double = 220.0

  /** Hue used for the positive half. */
  val PositiveHue: Double = 0.0

  private val Brightness = 0.95
  private val MaxSaturation = 0.85

  /** Maps a normalized value in `[0, 1]` (values outside are clamped) to a colour. */
  def color(normalized: Double): HsbColor = {
    val v = math.max(0.0, math.min(1.0, normalized))
    val offset = 2.0 * v - 1.0
    HsbColor(if (offset < 0.0) NegativeHue else PositiveHue, MaxSaturation * math.abs(offset), Brightness)
  }
}
