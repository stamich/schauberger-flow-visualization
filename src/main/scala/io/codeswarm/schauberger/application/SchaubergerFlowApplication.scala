package io.codeswarm.schauberger.application

import io.codeswarm.schauberger.diagnostics.{CrossSectionVelocityFieldSampler, ScalarFieldCalculator}
import io.codeswarm.schauberger.geometry.GeometryFactory
import io.codeswarm.schauberger.model.{GeometryParameters, SimulationParameters, VisualizationParameters}
import io.codeswarm.schauberger.physics.{AxialFlowForce, CompositeFlowForce, SecondaryFlowForce, SwirlForce, WallRepulsionForce}
import io.codeswarm.schauberger.physics.secondary.TwinVortexSecondaryFlow
import io.codeswarm.schauberger.physics.swirl.SwirlProfileFactory
import io.codeswarm.schauberger.simulation.{FlowMetricsCalculator, PipeBoundaryHandler, SecondaryFlowFieldSampler, SemiImplicitEulerIntegrator, SimulationEngine, UniformCrossSectionParticleGenerator}
import io.codeswarm.schauberger.ui.{SimulationController, SimulationView}
import io.codeswarm.schauberger.visualization.{HeatMapRenderer, ParticleRenderer, PipeRenderer, SimulationRenderer, TrailBuffer, TrailRenderer, VectorFieldRenderer}
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas

/** ScalaFX entry point wiring milestone 0.6 domain components to the UI. */
object SchaubergerFlowApplication extends JFXApp3 {

  /** Constructs the application graph, shows the primary stage and starts animation. */
  override def start(): Unit = {
    val secondaryModel = new TwinVortexSecondaryFlow
    val swirlProfileFactory = new SwirlProfileFactory
    val engine = new SimulationEngine(
      forceModel = CompositeFlowForce(Vector(
        new AxialFlowForce,
        new SwirlForce(swirlProfileFactory),
        new SecondaryFlowForce(secondaryModel),
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
      trailBuffer = new TrailBuffer,
      vectorFieldRenderer = new VectorFieldRenderer(new SecondaryFlowFieldSampler(secondaryModel)),
      heatMapRenderer = new HeatMapRenderer(new CrossSectionVelocityFieldSampler, new ScalarFieldCalculator)
    )
    val controller = new SimulationController(
      engine = engine,
      geometryFactory = new GeometryFactory,
      renderer = renderer,
      metricsCalculator = new FlowMetricsCalculator(secondaryModel),
      initialSimulation = SimulationParameters.Default,
      initialGeometry = GeometryParameters.Default,
      initialVisualization = VisualizationParameters.Default
    )
    val view = new SimulationView(canvas, controller)

    stage = new JFXApp3.PrimaryStage {
      title = "Schauberger Flow Visualization 0.6"
      scene = new Scene(1580.0, 1080.0) { root = view }
    }

    controller.renderCurrent()
    view.timer.start()
  }
}
