package io.codeswarm.schauberger.ui

import scalafx.animation.AnimationTimer
import scalafx.geometry.Insets
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.Label
import scalafx.scene.layout.{BorderPane, HBox}

/** Main ScalaFX view containing simulation Canvas, status metrics and controls. */
final class SimulationView(
    canvas: Canvas,
    controller: SimulationController
) extends BorderPane {
  private val fpsLabel = new Label("FPS: --")
  private val stateLabel = new Label("Running")
  private val axialLabel = new Label("Axial: --")
  private val tangentialLabel = new Label("Tangential: --")
  private val angularLabel = new Label("Angular: --")
  private val vorticityLabel = new Label("Vorticity proxy: --")
  private var lastTimestamp: Long = 0L

  private val status = new HBox {
    spacing = 16.0
    padding = Insets(8.0, 14.0, 8.0, 14.0)
    children = Seq(
      new Label("Schauberger Flow Visualization 0.2"),
      fpsLabel,
      stateLabel,
      axialLabel,
      tangentialLabel,
      angularLabel,
      vorticityLabel
    )
  }

  private val controls = new ControlPanel(controller, () => refreshStatus())

  padding = Insets(8.0)
  top = status
  center = canvas
  bottom = controls

  /** Animation timer converting render timestamps to fixed-timestep simulation updates. */
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

  /** Refreshes FPS, running state and aggregate flow metrics. */
  private def refreshStatus(): Unit = {
    val metrics = controller.metrics
    fpsLabel.text = if (controller.fps > 0.0) f"FPS: ${controller.fps}%.1f" else "FPS: --"
    stateLabel.text = if (controller.isRunning) "Running" else "Paused"
    axialLabel.text = f"Axial: ${metrics.meanAxialVelocity}%.1f"
    tangentialLabel.text = f"Tangential: ${metrics.meanTangentialVelocity}%.1f"
    angularLabel.text = f"Angular: ${metrics.meanAngularVelocity}%.3f"
    vorticityLabel.text = f"Vorticity proxy: ${metrics.vorticityProxy}%.3f"
  }
}
