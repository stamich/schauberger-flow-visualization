package io.codeswarm.schauberger.ui

import io.codeswarm.schauberger.model.{ViewMode, VisualizationParameters}
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{CheckBox, ComboBox, Label}
import scalafx.scene.layout.VBox

/** Controls view mode, fading trails, slice position and vector overlay. */
final class VisualizationControlPane(
    controller: SimulationController,
    onStatusRefresh: () => Unit
) extends VBox {
  spacing = 6.0
  padding = Insets(8.0)

  private val viewCombo = new ComboBox[String](ObservableBuffer("Longitudinal", "Cross section")) {
    value = if (controller.visualizationParameters.viewMode == ViewMode.CrossSection) "Cross section" else "Longitudinal"
    prefWidth = 145.0
  }

  private val trailLengthField = new NumericSliderField("Trail samples:", 0.0, 100.0, controller.visualizationParameters.trailLength, 20.0, 0, integerValue = true, onValueChanged = value => update(controller.visualizationParameters.copy(trailLength = value.toInt)))
  private val trailAgeField = new NumericSliderField("Trail fade time:", 0.2, 5.0, controller.visualizationParameters.trailDurationSeconds, 1.0, 1, onValueChanged = value => update(controller.visualizationParameters.copy(trailDurationSeconds = value)))
  private val sliceField = new NumericSliderField("Cross-section x:", 0.0, 1.0, controller.visualizationParameters.crossSectionFraction, 0.25, 2, onValueChanged = value => update(controller.visualizationParameters.copy(crossSectionFraction = value)))
  private val vectorResolutionField = new NumericSliderField("Vector grid:", 5.0, 31.0, controller.visualizationParameters.vectorFieldResolution, 5.0, 0, integerValue = true, onValueChanged = value => update(controller.visualizationParameters.copy(vectorFieldResolution = value.toInt)))

  private val showParticles = new CheckBox("Show particles") { selected = controller.visualizationParameters.showParticles }
  private val showTrails = new CheckBox("Show trails") { selected = controller.visualizationParameters.showTrails }
  private val showVectors = new CheckBox("Show secondary vector field") { selected = controller.visualizationParameters.showSecondaryVectors }

  children = Seq(
    new Label("Visualization"), new Label("View:"), viewCombo,
    trailLengthField, trailAgeField, sliceField,
    showParticles, showTrails, showVectors, vectorResolutionField
  )

  viewCombo.value.onChange { (_, _, next) =>
    update(controller.visualizationParameters.copy(viewMode = if (next == "Cross section") ViewMode.CrossSection else ViewMode.Longitudinal))
  }
  showParticles.selected.onChange { (_, _, enabled) => update(controller.visualizationParameters.copy(showParticles = enabled)) }
  showTrails.selected.onChange { (_, _, enabled) => update(controller.visualizationParameters.copy(showTrails = enabled)) }
  showVectors.selected.onChange { (_, _, enabled) => update(controller.visualizationParameters.copy(showSecondaryVectors = enabled)) }

  /** Applies render-only changes and refreshes the current frame. */
  private def update(next: VisualizationParameters): Unit = {
    controller.updateVisualizationParameters(next)
    onStatusRefresh()
  }
}
