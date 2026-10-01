package io.codeswarm.schauberger.ui

import io.codeswarm.schauberger.model._
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, ComboBox, Label, Slider}
import scalafx.scene.layout.{HBox, VBox}

/** Milestone 0.3 controls for flow, geometry, slice position and fading trails.
  *
  * Population changes are applied on reset because particle count is stateful. Most
  * physical and visualization values are live. Geometry changes rebuild the domain
  * geometry and reset particles to preserve containment invariants.
  */
final class ControlPanel(
    controller: SimulationController,
    onStatusRefresh: () => Unit
) extends VBox {

  private val particles = slider(100.0, 4000.0, controller.simulationParameters.particleCount.toDouble, 500.0)
  private val axial = slider(20.0, 180.0, controller.simulationParameters.axialVelocity, 40.0)
  private val angular = slider(0.0, 1.5, controller.simulationParameters.angularVelocity, 0.25)
  private val response = slider(0.2, 6.0, controller.simulationParameters.swirlResponse, 1.0)
  private val trailLength = slider(0.0, 100.0, controller.visualizationParameters.trailLength.toDouble, 20.0)
  private val trailAge = slider(0.2, 5.0, controller.visualizationParameters.trailDurationSeconds, 1.0)
  private val widthSlider = slider(150.0, 320.0, controller.geometryParameters.width, 40.0)
  private val heightSlider = slider(150.0, 340.0, controller.geometryParameters.height, 40.0)
  private val asymmetry = slider(-0.30, 0.30, controller.geometryParameters.asymmetry, 0.10)
  private val twist = slider(0.0, 3.0, controller.geometryParameters.twistTurns, 0.5)
  private val slice = slider(0.0, 1.0, controller.visualizationParameters.crossSectionFraction, 0.25)

  private val particleValue = new Label(controller.simulationParameters.particleCount.toString)
  private val axialValue = new Label(f"${controller.simulationParameters.axialVelocity}%.1f")
  private val angularValue = new Label(f"${controller.simulationParameters.angularVelocity}%.2f")
  private val responseValue = new Label(f"${controller.simulationParameters.swirlResponse}%.2f")
  private val trailLengthValue = new Label(controller.visualizationParameters.trailLength.toString)
  private val trailAgeValue = new Label(f"${controller.visualizationParameters.trailDurationSeconds}%.1fs")
  private val widthValue = new Label(f"${controller.geometryParameters.width}%.0f")
  private val heightValue = new Label(f"${controller.geometryParameters.height}%.0f")
  private val asymmetryValue = new Label(f"${controller.geometryParameters.asymmetry}%.2f")
  private val twistValue = new Label(f"${controller.geometryParameters.twistTurns}%.2f")
  private val sliceValue = new Label(f"${controller.visualizationParameters.crossSectionFraction * 100.0}%.0f%%")

  private val viewCombo = new ComboBox[String](ObservableBuffer("Longitudinal", "Cross section")) {
    value = "Longitudinal"
    prefWidth = 145.0
  }
  private val rotationCombo = new ComboBox[String](ObservableBuffer("Counter-clockwise", "Clockwise")) {
    value = "Counter-clockwise"
    prefWidth = 170.0
  }
  private val geometryCombo = new ComboBox[String](ObservableBuffer("Circular", "Ovoid", "Twisted ovoid")) {
    value = "Twisted ovoid"
    prefWidth = 150.0
  }

  spacing = 8.0
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
            applyPopulation()
            controller.reset()
            controller.renderCurrent()
            onStatusRefresh()
          }
        },
        new Label("View:"), viewCombo,
        new Label("Geometry:"), geometryCombo,
        new Label("Rotation:"), rotationCombo
      )
    },
    row("Particles:", particles, particleValue),
    row("Axial velocity:", axial, axialValue),
    row("Angular velocity:", angular, angularValue),
    row("Swirl response:", response, responseValue),
    row("Trail length:", trailLength, trailLengthValue),
    row("Trail fade time:", trailAge, trailAgeValue),
    row("Pipe width:", widthSlider, widthValue),
    row("Pipe height:", heightSlider, heightValue),
    row("Ovoid asymmetry:", asymmetry, asymmetryValue),
    row("Twist turns:", twist, twistValue),
    row("Cross-section x:", slice, sliceValue)
  )

  particleValueBinding()
  physicalBindings()
  visualizationBindings()
  selectionBindings()

  /** Installs the deferred particle-count display binding. */
  private def particleValueBinding(): Unit =
    particles.value.onChange { (_, _, next) => particleValue.text = next.intValue().toString }

  /** Installs live bindings for physical flow controls. */
  private def physicalBindings(): Unit = {
    axial.value.onChange { (_, _, next) =>
      axialValue.text = f"${next.doubleValue()}%.1f"
      updateSimulation(controller.simulationParameters.copy(axialVelocity = next.doubleValue()))
    }
    angular.value.onChange { (_, _, next) =>
      angularValue.text = f"${next.doubleValue()}%.2f"
      updateSimulation(controller.simulationParameters.copy(angularVelocity = next.doubleValue()))
    }
    response.value.onChange { (_, _, next) =>
      responseValue.text = f"${next.doubleValue()}%.2f"
      updateSimulation(controller.simulationParameters.copy(swirlResponse = next.doubleValue()))
    }
    rotationCombo.value.onChange { (_, _, next) =>
      val direction = if (next == "Clockwise") RotationDirection.Clockwise else RotationDirection.CounterClockwise
      updateSimulation(controller.simulationParameters.copy(rotationDirection = direction))
    }
  }

  /** Installs live bindings for render-only trail and slice controls. */
  private def visualizationBindings(): Unit = {
    trailLength.value.onChange { (_, _, next) =>
      trailLengthValue.text = next.intValue().toString
      updateVisualization(controller.visualizationParameters.copy(trailLength = next.intValue()))
    }
    trailAge.value.onChange { (_, _, next) =>
      trailAgeValue.text = f"${next.doubleValue()}%.1fs"
      updateVisualization(controller.visualizationParameters.copy(trailDurationSeconds = next.doubleValue()))
    }
    slice.value.onChange { (_, _, next) =>
      sliceValue.text = f"${next.doubleValue() * 100.0}%.0f%%"
      updateVisualization(controller.visualizationParameters.copy(crossSectionFraction = next.doubleValue()))
    }
    viewCombo.value.onChange { (_, _, next) =>
      val mode = if (next == "Cross section") ViewMode.CrossSection else ViewMode.Longitudinal
      updateVisualization(controller.visualizationParameters.copy(viewMode = mode))
    }
  }

  /** Installs geometry selection/twist bindings that intentionally reset the particle state. */
  private def selectionBindings(): Unit = {
    widthSlider.value.onChange { (_, _, next) =>
      widthValue.text = f"${next.doubleValue()}%.0f"
      updateGeometry(controller.geometryParameters.copy(width = next.doubleValue()))
    }
    heightSlider.value.onChange { (_, _, next) =>
      heightValue.text = f"${next.doubleValue()}%.0f"
      updateGeometry(controller.geometryParameters.copy(height = next.doubleValue()))
    }
    asymmetry.value.onChange { (_, _, next) =>
      asymmetryValue.text = f"${next.doubleValue()}%.2f"
      updateGeometry(controller.geometryParameters.copy(asymmetry = next.doubleValue()))
    }
    twist.value.onChange { (_, _, next) =>
      twistValue.text = f"${next.doubleValue()}%.2f"
      if (controller.geometryParameters.geometryType == GeometryType.TwistedOvoid)
        updateGeometry(controller.geometryParameters.copy(twistTurns = next.doubleValue()))
    }
    geometryCombo.value.onChange { (_, _, next) =>
      val geometryType = next match {
        case "Circular" => GeometryType.Circular
        case "Ovoid" => GeometryType.Ovoid
        case _ => GeometryType.TwistedOvoid
      }
      updateGeometry(controller.geometryParameters.copy(geometryType = geometryType))
    }
  }

  /** Creates a consistently styled numeric slider. */
  private def slider(min: Double, max: Double, initial: Double, major: Double): Slider = new Slider(min, max, initial) {
    majorTickUnit = major
    minorTickCount = 4
    showTickLabels = true
    showTickMarks = true
    prefWidth = 300.0
  }

  /** Creates one labeled horizontal control row. */
  private def row(label: String, control: Slider, value: Label): HBox = new HBox {
    spacing = 10.0
    alignment = Pos.CenterLeft
    children = Seq(new Label(label) { minWidth = 125.0 }, control, value)
  }

  /** Applies a live physical parameter change. */
  private def updateSimulation(next: SimulationParameters): Unit = {
    controller.updateSimulationParameters(next)
    onStatusRefresh()
  }

  /** Applies a render-only parameter change. */
  private def updateVisualization(next: VisualizationParameters): Unit = {
    controller.updateVisualizationParameters(next)
    onStatusRefresh()
  }

  /** Replaces active geometry and refreshes the newly reset state. */
  private def updateGeometry(next: GeometryParameters): Unit = {
    controller.updateGeometryParameters(next)
    controller.renderCurrent()
    onStatusRefresh()
  }

  /** Applies selected population size immediately before an explicit reset. */
  private def applyPopulation(): Unit =
    controller.updateSimulationParameters(controller.simulationParameters.copy(particleCount = particles.value.value.toInt))
}
