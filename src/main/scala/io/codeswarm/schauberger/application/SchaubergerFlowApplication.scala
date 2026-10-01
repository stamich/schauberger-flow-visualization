package io.codeswarm.schauberger.application

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.model.SimulationParameters
import io.codeswarm.schauberger.physics.{AxialFlowForce, CompositeFlowForce, SolidBodySwirlProfile, SwirlForce, WallRepulsionForce}
import io.codeswarm.schauberger.simulation.{FlowMetricsCalculator, PipeBoundaryHandler, SemiImplicitEulerIntegrator, SimulationEngine, UniformInletParticleGenerator}
import io.codeswarm.schauberger.ui.{SimulationController, SimulationView}
import io.codeswarm.schauberger.visualization.{ParticleRenderer, PipeRenderer, SimulationRenderer, TrailBuffer, TrailRenderer}
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas

/** ScalaFX entry point wiring milestone 0.2 domain services to the user interface. */
object SchaubergerFlowApplication extends JFXApp3 {

  /** Constructs the complete application graph and starts the animation timer. */
  override def start(): Unit = {
    val geometry = StraightCircularPipe(length = 1000.0, radius = 120.0)
    val parameters = SimulationParameters.Default
    val forceModel = CompositeFlowForce(Vector(
      new AxialFlowForce,
      new SwirlForce(new SolidBodySwirlProfile),
      new WallRepulsionForce
    ))
    val engine = new SimulationEngine(
      geometry = geometry,
      forceModel = forceModel,
      integrator = new SemiImplicitEulerIntegrator,
      particleGenerator = new UniformInletParticleGenerator(seed = 42L),
      boundaryHandler = new PipeBoundaryHandler
    )

    val canvas = new Canvas(1280.0, 620.0)
    val renderer = new SimulationRenderer(
      canvas,
      new PipeRenderer,
      new ParticleRenderer,
      new TrailRenderer,
      new TrailBuffer
    )
    val controller = new SimulationController(
      engine,
      geometry,
      renderer,
      new FlowMetricsCalculator,
      parameters
    )
    val view = new SimulationView(canvas, controller)

    stage = new JFXApp3.PrimaryStage {
      title = "Schauberger Flow Visualization 0.2"
      scene = new Scene(1360.0, 820.0) {
        root = view
      }
    }
    controller.renderCurrent()
    view.timer.start()
  }
}
