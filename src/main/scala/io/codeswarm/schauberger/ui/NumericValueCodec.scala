package io.codeswarm.schauberger.ui

/** Pure parsing, clamping, rounding and formatting logic used by editable slider fields. */
object NumericValueCodec {
  /** Parses decimal text, accepting either dot or comma as decimal separator. */
  def parse(text: String): Option[Double] =
    scala.util.Try(text.trim.replace(',', '.').toDouble).toOption.filter(value => !value.isNaN && !value.isInfinity)

  /** Clamps a value to `[min,max]` and optionally rounds it to the nearest integer. */
  def normalize(value: Double, min: Double, max: Double, integer: Boolean): Double = {
    require(max > min)
    val clamped = math.max(min, math.min(max, value))
    if (integer) math.rint(clamped) else clamped
  }

  /** Formats a normalized value for editing with a stable US decimal separator. */
  def format(value: Double, decimals: Int, integer: Boolean): String = {
    require(decimals >= 0)
    if (integer) value.round.toString
    else String.format(java.util.Locale.US, s"%.${decimals}f", Double.box(value))
  }
}
