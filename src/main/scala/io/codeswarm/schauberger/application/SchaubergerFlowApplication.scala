package io.codeswarm.schauberger.application

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.model.SimulationParameters
import io.codeswarm.schauberger.physics.{AxialFlowForce, CompositeFlowForce, WallRepulsionForce}
import io.codeswarm.schauberger.simulation.{PipeBoundaryHandler, SemiImplicitEulerIntegrator, SimulationEngine, UniformInletParticleGenerator}
import io.codeswarm.schauberger.ui.{SimulationController, SimulationView}
import io.codeswarm.schauberger.visualization.{ParticleRenderer, PipeRenderer, SimulationRenderer}
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas

/** Application bootstrap for Schauberger Flow Visualization milestone 0.1.
  *
  * The bootstrap wires dependencies only; physical calculations live in lower layers.
  */
object SchaubergerFlowApplication extends JFXApp3 {

  /** Creates the application graph and starts the ScalaFX animation timer. */
  override def start(): Unit = {
    val geometry = StraightCircularPipe(length = 1000.0, radius = 120.0)
    val parameters = SimulationParameters.Default
    val forces = CompositeFlowForce(Vector(new AxialFlowForce, new WallRepulsionForce))
    val engine = new SimulationEngine(
      geometry = geometry,
      forceModel = forces,
      integrator = new SemiImplicitEulerIntegrator,
      particleGenerator = new UniformInletParticleGenerator(seed = 42L),
      boundaryHandler = new PipeBoundaryHandler
    )

    val canvas = new Canvas(width = 1320.0, height = 620.0)
    val renderer = new SimulationRenderer(canvas, new PipeRenderer, new ParticleRenderer)
    val controller = new SimulationController(engine, geometry, renderer, parameters)
    val view = new SimulationView(canvas, controller)

    stage = new JFXApp3.PrimaryStage {
      title = "Schauberger Flow Visualization - Milestone 0.1"
      scene = new Scene(1380.0, 760.0) {
        root = view
      }
      minWidth = 1100.0
      minHeight = 680.0
    }

    controller.renderCurrent()
    view.timer.start()
  }
}
