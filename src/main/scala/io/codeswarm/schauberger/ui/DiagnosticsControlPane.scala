package io.codeswarm.schauberger.ui

import io.codeswarm.schauberger.diagnostics.FieldType
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{CheckBox, ComboBox, Label}
import scalafx.scene.layout.VBox

/** Controls milestone 0.5 scalar heat maps and diagnostic overlays. */
final class DiagnosticsControlPane(
    controller: SimulationController,
    onStatusRefresh: () => Unit
) extends VBox {
  spacing = 6.0
  padding = Insets(8.0)

  private val showHeatMap = new CheckBox("Show heat map") {
    selected = controller.visualizationParameters.showHeatMap
  }

  private val fieldCombo = new ComboBox[String](ObservableBuffer(FieldType.values.map(_.displayName): _*)) {
    value = controller.visualizationParameters.heatMapField.displayName
    prefWidth = 170.0
  }

  private val resolutionField = new NumericSliderField(
    "Heat-map grid:", 5.0, 60.0, controller.visualizationParameters.heatMapResolution,
    5.0, 0, integerValue = true,
    onValueChanged = value => update(controller.visualizationParameters.copy(heatMapResolution = value.toInt))
  )

  children = Seq(new Label("Diagnostics"), showHeatMap, new Label("Field:"), fieldCombo, resolutionField)

  showHeatMap.selected.onChange { (_, _, enabled) =>
    update(controller.visualizationParameters.copy(showHeatMap = enabled))
  }

  fieldCombo.value.onChange { (_, _, next) =>
    update(controller.visualizationParameters.copy(heatMapField = FieldType.fromDisplayName(next)))
  }

  /** Applies render-only diagnostic settings. */
  private def update(next: io.codeswarm.schauberger.model.VisualizationParameters): Unit = {
    controller.updateVisualizationParameters(next)
    onStatusRefresh()
  }
}
