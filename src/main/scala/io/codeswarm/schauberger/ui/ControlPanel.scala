package io.codeswarm.schauberger.ui

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Button
import scalafx.scene.layout.{HBox, VBox}

/** Composite milestone 0.5 control panel built from focused sub-panes.
  *
  * Every numeric slider is paired with editable numeric input through
  * [[NumericSliderField]].
  */
final class ControlPanel(controller: SimulationController, onStatusRefresh: () => Unit) extends VBox {
  spacing = 8.0
  padding = Insets(8.0)

  private val actions = new HBox {
    spacing = 10.0
    alignment = Pos.CenterLeft
    children = Seq(
      new Button("Start") { onAction = _ => controller.start() },
      new Button("Pause") { onAction = _ => controller.pause() },
      new Button("Reset") {
        onAction = _ => {
          controller.reset()
          controller.renderCurrent()
          onStatusRefresh()
        }
      }
    )
  }

  private val sections = new HBox {
    spacing = 16.0
    children = Seq(
      new FlowControlPane(controller, onStatusRefresh),
      new GeometryControlPane(controller, onStatusRefresh),
      new VisualizationControlPane(controller, onStatusRefresh),
      new DiagnosticsControlPane(controller, onStatusRefresh)
    )
  }

  children = Seq(actions, sections)
}
