package io.codeswarm.schauberger.application

import io.codeswarm.schauberger.geometry.GeometryFactory
import io.codeswarm.schauberger.model.{GeometryParameters, SimulationParameters, VisualizationParameters}
import io.codeswarm.schauberger.physics.{AxialFlowForce, CompositeFlowForce, SolidBodySwirlProfile, SwirlForce, WallRepulsionForce}
import io.codeswarm.schauberger.simulation.{FlowMetricsCalculator, PipeBoundaryHandler, SemiImplicitEulerIntegrator, SimulationEngine, UniformCrossSectionParticleGenerator}
import io.codeswarm.schauberger.ui.{SimulationController, SimulationView}
import io.codeswarm.schauberger.visualization.{ParticleRenderer, PipeRenderer, SimulationRenderer, TrailBuffer, TrailRenderer}
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas

/** ScalaFX entry point wiring milestone 0.3 domain components to the UI. */
object SchaubergerFlowApplication extends JFXApp3 {

  /** Constructs the application graph, shows the primary stage and starts animation. */
  override def start(): Unit = {
    val engine = new SimulationEngine(
      forceModel = CompositeFlowForce(Vector(
        new AxialFlowForce,
        new SwirlForce(new SolidBodySwirlProfile),
        new WallRepulsionForce
      )),
      integrator = new SemiImplicitEulerIntegrator,
      particleGenerator = new UniformCrossSectionParticleGenerator(42L),
      boundaryHandler = new PipeBoundaryHandler
    )

    val canvas = new Canvas(1280.0, 540.0)
    val renderer = new SimulationRenderer(
      canvas = canvas,
      pipeRenderer = new PipeRenderer,
      particleRenderer = new ParticleRenderer,
      trailRenderer = new TrailRenderer,
      trailBuffer = new TrailBuffer
    )
    val controller = new SimulationController(
      engine = engine,
      geometryFactory = new GeometryFactory,
      renderer = renderer,
      metricsCalculator = new FlowMetricsCalculator,
      initialSimulation = SimulationParameters.Default,
      initialGeometry = GeometryParameters.Default,
      initialVisualization = VisualizationParameters.Default
    )
    val view = new SimulationView(canvas, controller)

    stage = new JFXApp3.PrimaryStage {
      title = "Schauberger Flow Visualization 0.3"
      scene = new Scene(1380.0, 1020.0) {
        root = view
      }
    }

    controller.renderCurrent()
    view.timer.start()
  }
}
