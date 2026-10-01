package io.codeswarm.schauberger.ui

import io.codeswarm.schauberger.model.{GeometryParameters, GeometryType}
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{ComboBox, Label}
import scalafx.scene.layout.VBox

/** Controls pipe cross-section and twist geometry. */
final class GeometryControlPane(
    controller: SimulationController,
    onStatusRefresh: () => Unit
) extends VBox {

  spacing = 6.0
  padding = Insets(8.0)

  private val geometryCombo = new ComboBox[String](ObservableBuffer("Circular", "Ovoid", "Twisted ovoid")) {
    value = controller.geometryParameters.geometryType match {
      case GeometryType.Circular => "Circular"
      case GeometryType.Ovoid => "Ovoid"
      case GeometryType.TwistedOvoid => "Twisted ovoid"
    }
    prefWidth = 150.0
  }

  private val pipeWidthField = new NumericSliderField("Pipe width:", 150.0, 320.0, controller.geometryParameters.width, 40.0, 0, onValueChanged = value => updateGeometry(controller.geometryParameters.copy(width = value)))
  private val pipeHeightField = new NumericSliderField("Pipe height:", 150.0, 340.0, controller.geometryParameters.height, 40.0, 0, onValueChanged = value => updateGeometry(controller.geometryParameters.copy(height = value)))
  private val asymmetryField = new NumericSliderField("Ovoid asymmetry:", -0.30, 0.30, controller.geometryParameters.asymmetry, 0.10, 2, onValueChanged = value => updateGeometry(controller.geometryParameters.copy(asymmetry = value)))
  private val twistTurnsField = new NumericSliderField("Twist turns:", 0.0, 3.0, controller.geometryParameters.twistTurns, 0.5, 2, onValueChanged = value => {
    if (controller.geometryParameters.geometryType == GeometryType.TwistedOvoid)
      updateGeometry(controller.geometryParameters.copy(twistTurns = value))
  })

  children = Seq(
    new Label("Geometry"),
    new Label("Geometry type:"),
    geometryCombo,
    pipeWidthField,
    pipeHeightField,
    asymmetryField,
    twistTurnsField
  )

  geometryCombo.value.onChange { (_, _, next) =>
    val geometryType = next match {
      case "Circular" => GeometryType.Circular
      case "Ovoid" => GeometryType.Ovoid
      case _ => GeometryType.TwistedOvoid
    }
    updateGeometry(controller.geometryParameters.copy(geometryType = geometryType))
  }

  /** Rebuilds active geometry and refreshes the reset simulation. */
  private def updateGeometry(next: GeometryParameters): Unit = {
    controller.updateGeometryParameters(next)
    controller.renderCurrent()
    onStatusRefresh()
  }
}
