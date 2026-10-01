package io.codeswarm.schauberger.ui

import io.codeswarm.schauberger.model.{RotationDirection, SimulationParameters, ViewMode}
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, ComboBox, Label, Slider}
import scalafx.scene.layout.{HBox, VBox}

/** Interactive controls for milestone 0.2 particle count, axial flow, swirl and view. */
final class ControlPanel(
    controller: SimulationController,
    onStatusRefresh: () => Unit
) extends VBox {

  private val particleSlider = slider(100.0, 10000.0, controller.parameters.particleCount.toDouble, 2000.0)
  private val axialSlider = slider(20.0, 220.0, controller.parameters.axialVelocity, 50.0)
  private val swirlSlider = slider(0.0, 2.5, controller.parameters.angularVelocity, 0.5)
  private val swirlResponseSlider = slider(0.2, 8.0, controller.parameters.swirlResponse, 2.0)
  private val trailSlider = slider(0.0, 200.0, controller.parameters.trailLength.toDouble, 50.0)

  private val particleValue = new Label(controller.parameters.particleCount.toString)
  private val axialValue = new Label(f"${controller.parameters.axialVelocity}%.1f")
  private val swirlValue = new Label(f"${controller.parameters.angularVelocity}%.2f")
  private val swirlResponseValue = new Label(f"${controller.parameters.swirlResponse}%.2f")
  private val trailValue = new Label(controller.parameters.trailLength.toString)

  private val viewCombo = new ComboBox[String](ObservableBuffer("Longitudinal", "Cross section")) {
    value = "Longitudinal"
    prefWidth = 150.0
  }

  private val rotationCombo = new ComboBox[String](ObservableBuffer("Counter-clockwise", "Clockwise")) {
    value = "Counter-clockwise"
    prefWidth = 170.0
  }

  spacing = 9.0
  padding = Insets(10.0, 14.0, 12.0, 14.0)

  children = Seq(
    new HBox {
      spacing = 10.0
      alignment = Pos.CenterLeft
      children = Seq(
        new Button("Start") { onAction = _ => controller.start() },
        new Button("Pause") { onAction = _ => controller.pause() },
        new Button("Reset") {
          onAction = _ => {
            applyParticleCount()
            controller.reset()
            controller.renderCurrent()
            onStatusRefresh()
          }
        },
        new Label("View:"), viewCombo,
        new Label("Rotation:"), rotationCombo
      )
    },
    row("Particles:", particleSlider, particleValue),
    row("Axial velocity:", axialSlider, axialValue),
    row("Angular velocity:", swirlSlider, swirlValue),
    row("Swirl response:", swirlResponseSlider, swirlResponseValue),
    row("Trail length:", trailSlider, trailValue)
  )

  particleSlider.value.onChange { (_, _, next) => particleValue.text = next.intValue().toString }

  axialSlider.value.onChange { (_, _, next) =>
    axialValue.text = f"${next.doubleValue()}%.1f"
    update(controller.parameters.copy(axialVelocity = next.doubleValue()))
  }

  swirlSlider.value.onChange { (_, _, next) =>
    swirlValue.text = f"${next.doubleValue()}%.2f"
    update(controller.parameters.copy(angularVelocity = next.doubleValue()))
  }

  swirlResponseSlider.value.onChange { (_, _, next) =>
    swirlResponseValue.text = f"${next.doubleValue()}%.2f"
    update(controller.parameters.copy(swirlResponse = next.doubleValue()))
  }

  trailSlider.value.onChange { (_, _, next) =>
    trailValue.text = next.intValue().toString
    update(controller.parameters.copy(trailLength = next.intValue()))
  }

  viewCombo.value.onChange { (_, _, next) =>
    val mode = if (next == "Cross section") ViewMode.CrossSection else ViewMode.Longitudinal
    controller.setViewMode(mode)
    controller.renderCurrent()
  }

  rotationCombo.value.onChange { (_, _, next) =>
    val direction = if (next == "Clockwise") RotationDirection.Clockwise else RotationDirection.CounterClockwise
    update(controller.parameters.copy(rotationDirection = direction))
  }

  /** Creates one consistently configured parameter slider. */
  private def slider(min: Double, max: Double, initial: Double, major: Double): Slider = new Slider(min, max, initial) {
    majorTickUnit = major
    minorTickCount = 4
    showTickLabels = true
    showTickMarks = true
    prefWidth = 320.0
  }

  /** Creates one labeled horizontal parameter row. */
  private def row(label: String, control: Slider, value: Label): HBox = new HBox {
    spacing = 10.0
    alignment = Pos.CenterLeft
    children = Seq(new Label(label) { minWidth = 120.0 }, control, value)
  }

  /** Applies a parameter update and refreshes status-dependent UI. */
  private def update(next: SimulationParameters): Unit = {
    controller.updateParameters(next)
    onStatusRefresh()
  }

  /** Applies selected particle count before reset because population size is stateful. */
  private def applyParticleCount(): Unit =
    controller.updateParameters(controller.parameters.copy(particleCount = particleSlider.value.value.toInt))
}
