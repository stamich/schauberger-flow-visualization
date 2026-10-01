package io.codeswarm.schauberger.ui

import io.codeswarm.schauberger.model.SimulationParameters
import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, Slider}
import scalafx.scene.layout.{HBox, VBox}

/** ScalaFX control panel for milestone 0.1 simulation parameters and lifecycle. */
final class ControlPanel(
    controller: SimulationController,
    onStatusRefresh: () => Unit
) extends VBox {

  private val particleSlider = new Slider(100.0, 10000.0, controller.parameters.particleCount.toDouble) {
    majorTickUnit = 2000.0
    minorTickCount = 3
    showTickLabels = true
    showTickMarks = true
    blockIncrement = 100.0
    prefWidth = 280.0
  }

  private val velocitySlider = new Slider(20.0, 220.0, controller.parameters.axialVelocity) {
    majorTickUnit = 50.0
    minorTickCount = 4
    showTickLabels = true
    showTickMarks = true
    blockIncrement = 5.0
    prefWidth = 280.0
  }

  /** Label displaying the selected particle count; changes are applied on reset. */
  val particleValueLabel: Label = new Label(controller.parameters.particleCount.toString)

  /** Label displaying the currently selected target axial velocity. */
  val velocityValueLabel: Label = new Label(f"${controller.parameters.axialVelocity}%.1f")

  spacing = 10.0
  padding = Insets(10.0, 14.0, 12.0, 14.0)

  children = Seq(
    new HBox {
      spacing = 10.0
      alignment = Pos.CenterLeft
      children = Seq(
        new Button("Start") {
          onAction = _ => controller.start()
        },
        new Button("Pause") {
          onAction = _ => controller.pause()
        },
        new Button("Reset") {
          onAction = _ => {
            applyParticleCount()
            controller.reset()
            controller.renderCurrent()
            onStatusRefresh()
          }
        },
        new Label("Particles:"),
        particleSlider,
        particleValueLabel,
        new Label("Axial velocity:"),
        velocitySlider,
        velocityValueLabel
      )
    }
  )

  particleSlider.value.onChange { (_, _, next) =>
    particleValueLabel.text = next.intValue().toString
  }

  velocitySlider.value.onChange { (_, _, next) =>
    velocityValueLabel.text = f"${next.doubleValue()}%.1f"
    val current = controller.parameters
    controller.updateParameters(current.copy(axialVelocity = next.doubleValue()))
  }

  /** Applies the selected particle count to parameters before a state reset. */
  private def applyParticleCount(): Unit = {
    val current: SimulationParameters = controller.parameters
    controller.updateParameters(current.copy(particleCount = particleSlider.value.value.toInt))
  }
}
