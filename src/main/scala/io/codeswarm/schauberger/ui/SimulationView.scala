package io.codeswarm.schauberger.ui

import scalafx.animation.AnimationTimer
import scalafx.geometry.Insets
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.Label
import scalafx.scene.layout.{BorderPane, HBox}

/** Main ScalaFX composition containing status, Canvas and milestone 0.6 controls. */
final class SimulationView(
    canvas: Canvas,
    controller: SimulationController
) extends BorderPane {
  private val fpsLabel = new Label("FPS: --")
  private val stateLabel = new Label("Running")
  private val geometryLabel = new Label("Geometry: --")
  private val axialLabel = new Label("Axial: --")
  private val tangentialLabel = new Label("Tangential: --")
  private val secondaryLabel = new Label("Secondary: --")
  private val energyLabel = new Label("Secondary ratio: --")
  private val twistLabel = new Label("Twist: --")
  private val profileLabel = new Label("Profile: --")
  private var lastTimestamp = 0L

  private val status = new HBox {
    spacing = 12.0
    padding = Insets(8.0, 14.0, 8.0, 14.0)
    children = Seq(
      new Label("Schauberger Flow Visualization 0.6"),
      fpsLabel,
      stateLabel,
      geometryLabel,
      axialLabel,
      tangentialLabel,
      profileLabel,
      secondaryLabel,
      energyLabel,
      twistLabel
    )
  }

  private val controls = new ControlPanel(controller, () => refreshStatus())

  padding = Insets(8.0)
  top = status
  center = canvas
  bottom = controls

  /** Animation timer converting render timestamps into fixed-step simulation updates. */
  val timer: AnimationTimer = AnimationTimer { now =>
    if (lastTimestamp == 0L) {
      lastTimestamp = now
      controller.renderCurrent()
    } else {
      val frameSeconds = (now - lastTimestamp).toDouble / 1_000_000_000.0
      lastTimestamp = now
      controller.onFrame(frameSeconds)
      refreshStatus()
    }
  }

  /** Refreshes display-only status and aggregate flow metrics. */
  private def refreshStatus(): Unit = {
    val metrics = controller.metrics
    fpsLabel.text = if (controller.fps > 0.0) f"FPS: ${controller.fps}%.1f" else "FPS: --"
    stateLabel.text = if (controller.isRunning) "Running" else "Paused"
    geometryLabel.text = s"Geometry: ${controller.geometryParameters.geometryType}"
    axialLabel.text = f"Axial: ${metrics.meanAxialVelocity}%.1f"
    tangentialLabel.text = f"Tangential: ${metrics.meanTangentialVelocity}%.1f"
    profileLabel.text = s"Profile: ${controller.simulationParameters.swirl.profile.id}"
    secondaryLabel.text = f"Secondary: ${metrics.meanSecondaryVelocity}%.2f"
    energyLabel.text = f"Secondary ratio: ${metrics.secondaryFlowEnergyRatio * 100.0}%.1f%%"
    twistLabel.text = f"Twist rate: ${controller.geometry.twistRate}%.4f"
  }
}
