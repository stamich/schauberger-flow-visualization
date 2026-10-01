package io.codeswarm.schauberger.ui

import scalafx.animation.AnimationTimer
import scalafx.geometry.Insets
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.Label
import scalafx.scene.layout.{BorderPane, HBox, Priority}

/** Main ScalaFX view containing Canvas, status line, controls, and animation timer. */
final class SimulationView(
    canvas: Canvas,
    controller: SimulationController
) extends BorderPane {

  private val fpsLabel = new Label("FPS: --")
  private val stateLabel = new Label("Running")
  private var lastTimestamp: Long = 0L

  private val status = new HBox {
    spacing = 18.0
    padding = Insets(8.0, 14.0, 8.0, 14.0)
    children = Seq(
      new Label("Schauberger Flow Visualization 0.1"),
      fpsLabel,
      stateLabel
    )
  }

  private val controls = new ControlPanel(controller, () => refreshStatus())

  padding = Insets(8.0)
  top = status
  center = canvas
  bottom = controls

  BorderPane.setMargin(canvas, Insets(4.0, 0.0, 4.0, 0.0))
  HBox.setHgrow(canvas, Priority.Always)

  /** Animation timer translating display-frame timestamps to fixed-timestep updates. */
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

  /** Updates lightweight status labels from the controller. */
  private def refreshStatus(): Unit = {
    fpsLabel.text = if (controller.fps > 0.0) f"FPS: ${controller.fps}%.1f" else "FPS: --"
    stateLabel.text = if (controller.isRunning) "Running" else "Paused"
  }
}
