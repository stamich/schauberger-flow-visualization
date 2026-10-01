package io.codeswarm.schauberger.ui

import io.codeswarm.schauberger.model.RotationDirection
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{CheckBox, ComboBox, Label}
import scalafx.scene.layout.VBox

/** Controls particle population and physical flow parameters. */
final class FlowControlPane(
    controller: SimulationController,
    onStatusRefresh: () => Unit
) extends VBox {

  spacing = 6.0
  padding = Insets(8.0)

  private val particleField = new NumericSliderField("Particles:", 100.0, 4000.0, controller.simulationParameters.particleCount, 500.0, 0, integerValue = true, onValueChanged = value => {
    updateSimulation(controller.simulationParameters.copy(particleCount = value.toInt))
  })

  private val axialVelocityField = new NumericSliderField("Axial velocity:", 20.0, 180.0, controller.simulationParameters.axial.velocity, 40.0, 1, onValueChanged = value => {
    updateSimulation(controller.simulationParameters.copy(axial = controller.simulationParameters.axial.copy(velocity = value)))
  })

  private val angularVelocityField = new NumericSliderField("Angular velocity:", 0.0, 1.5, controller.simulationParameters.swirl.angularVelocity, 0.25, 2, onValueChanged = value => {
    updateSimulation(controller.simulationParameters.copy(swirl = controller.simulationParameters.swirl.copy(angularVelocity = value)))
  })

  private val swirlResponseField = new NumericSliderField("Swirl response:", 0.2, 6.0, controller.simulationParameters.swirl.response, 1.0, 2, onValueChanged = value => {
    updateSimulation(controller.simulationParameters.copy(swirl = controller.simulationParameters.swirl.copy(response = value)))
  })

  private val secondaryStrengthField = new NumericSliderField("Secondary strength:", 0.0, 60.0, controller.simulationParameters.secondaryFlow.strength, 10.0, 1, onValueChanged = value => {
    updateSimulation(controller.simulationParameters.copy(secondaryFlow = controller.simulationParameters.secondaryFlow.copy(strength = value)))
  })

  private val secondaryResponseField = new NumericSliderField("Secondary response:", 0.0, 6.0, controller.simulationParameters.secondaryFlow.response, 1.0, 2, onValueChanged = value => {
    updateSimulation(controller.simulationParameters.copy(secondaryFlow = controller.simulationParameters.secondaryFlow.copy(response = value)))
  })

  private val boundaryFadeField = new NumericSliderField("Boundary fade:", 0.0, 50.0, controller.simulationParameters.secondaryFlow.boundaryFadeDistance, 10.0, 1, onValueChanged = value => {
    updateSimulation(controller.simulationParameters.copy(secondaryFlow = controller.simulationParameters.secondaryFlow.copy(boundaryFadeDistance = value)))
  })

  private val secondaryEnabled = new CheckBox("Geometry-induced secondary flow") {
    selected = controller.simulationParameters.secondaryFlow.enabled
  }

  private val rotationCombo = new ComboBox[String](ObservableBuffer("Counter-clockwise", "Clockwise")) {
    value = if (controller.simulationParameters.swirl.rotationDirection == RotationDirection.Clockwise) "Clockwise" else "Counter-clockwise"
    prefWidth = 170.0
  }

  children = Seq(
    new Label("Flow"),
    particleField,
    axialVelocityField,
    angularVelocityField,
    swirlResponseField,
    secondaryEnabled,
    secondaryStrengthField,
    secondaryResponseField,
    boundaryFadeField,
    new Label("Rotation:"),
    rotationCombo
  )

  secondaryEnabled.selected.onChange { (_, _, enabled) =>
    updateSimulation(controller.simulationParameters.copy(secondaryFlow = controller.simulationParameters.secondaryFlow.copy(enabled = enabled)))
  }

  rotationCombo.value.onChange { (_, _, next) =>
    val direction = if (next == "Clockwise") RotationDirection.Clockwise else RotationDirection.CounterClockwise
    updateSimulation(controller.simulationParameters.copy(swirl = controller.simulationParameters.swirl.copy(rotationDirection = direction)))
  }

  /** Applies a live physical parameter change. */
  private def updateSimulation(next: io.codeswarm.schauberger.model.SimulationParameters): Unit = {
    controller.updateSimulationParameters(next)
    onStatusRefresh()
  }
}
