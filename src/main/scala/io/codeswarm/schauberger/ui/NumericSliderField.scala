package io.codeswarm.schauberger.ui

import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.control.{Label, Slider, TextField}
import scalafx.scene.layout.HBox

/** Reusable numeric control combining a slider with an editable text field.
  *
  * Both interaction paths are synchronized. Text input is parsed on Enter or when
  * focus leaves the field, clamped to the slider range and optionally rounded to
  * an integer. Invalid input is discarded and the last valid value is restored.
  */
final class NumericSliderField(
    labelText: String,
    minValue: Double,
    maxValue: Double,
    initialValue: Double,
    majorTickUnitValue: Double,
    decimals: Int,
    integerValue: Boolean = false,
    onValueChanged: Double => Unit
) extends HBox {

  require(maxValue > minValue)
  require(majorTickUnitValue > 0.0)
  require(decimals >= 0)

  private var internalUpdate = false

  /** Slider used for mouse/keyboard adjustment. */
  val sliderControl: Slider = new Slider(minValue, maxValue, normalize(initialValue)) {
    majorTickUnit = majorTickUnitValue
    minorTickCount = 4
    showTickLabels = true
    showTickMarks = true
    prefWidth = 285.0
  }

  /** Editable numeric field synchronized with [[sliderControl]]. */
  val textControl: TextField = new TextField {
    text = format(normalize(initialValue))
    prefColumnCount = math.max(5, decimals + 4)
    maxWidth = 90.0
  }

  spacing = 10.0
  alignment = Pos.CenterLeft
  children = Seq(
    new Label(labelText) { minWidth = 145.0 },
    sliderControl,
    textControl
  )

  sliderControl.value.onChange { (_, _, next) =>
    if (!internalUpdate) applyValue(next.doubleValue(), notify = true)
  }

  textControl.onAction = _ => commitText()
  textControl.focused.onChange { (_, _, focused) =>
    if (!focused) commitText()
  }

  /** Returns the current normalized numeric value. */
  def value: Double = normalize(sliderControl.value.value)

  /** Programmatically updates both controls and optionally notifies the owner. */
  def setValue(next: Double, notify: Boolean = false): Unit = applyValue(next, notify)

  /** Parses and commits user-entered text. */
  private def commitText(): Unit = {
    val parsed = NumericValueCodec.parse(textControl.text.value)
    parsed match {
      case Some(number) => applyValue(number, notify = true)
      case None => textControl.text = format(value)
    }
  }

  /** Applies clamping/rounding and keeps slider/text state consistent. */
  private def applyValue(raw: Double, notify: Boolean): Unit = {
    val next = normalize(raw)
    internalUpdate = true
    try {
      if (math.abs(sliderControl.value.value - next) > 1e-12) sliderControl.value = next
      textControl.text = format(next)
    } finally internalUpdate = false
    if (notify) onValueChanged(next)
  }

  /** Clamps to the configured range and optionally rounds to an integer. */
  private def normalize(raw: Double): Double = {
    NumericValueCodec.normalize(raw, minValue, maxValue, integerValue)
  }

  /** Formats the normalized value for editable display. */
  private def format(value: Double): String = NumericValueCodec.format(value, decimals, integerValue)
}
