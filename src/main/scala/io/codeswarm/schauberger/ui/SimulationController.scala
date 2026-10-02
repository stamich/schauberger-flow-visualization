package io.codeswarm.schauberger.ui

import io.codeswarm.schauberger.geometry.{GeometryFactory, PipeGeometry}
import io.codeswarm.schauberger.model._
import io.codeswarm.schauberger.simulation.{FlowMetricsCalculator, SimulationEngine}
import io.codeswarm.schauberger.visualization.SimulationRenderer

/** Coordinates immutable simulation state, replaceable geometry and ScalaFX rendering.
 *
 * The controller owns UI/runtime mutability. The physical engine itself remains
 * independent of ScalaFX and from any concrete pipe shape.
 */
final class SimulationController(
                                  engine: SimulationEngine,
                                  geometryFactory: GeometryFactory,
                                  renderer: SimulationRenderer,
                                  metricsCalculator: FlowMetricsCalculator,
                                  initialSimulation: SimulationParameters,
                                  initialGeometry: GeometryParameters,
                                  initialVisualization: VisualizationParameters
                                ) {
  private var simulationValue = initialSimulation
  private var geometryParametersValue = initialGeometry
  private var visualizationValue = initialVisualization
  private var geometryValue: PipeGeometry = geometryFactory.create(initialGeometry)
  private var stateValue = engine.initialState(initialSimulation, geometryValue)
  private var runningValue = true
  private var accumulator = 0.0
  private var fpsElapsed = 0.0
  private var fpsFrames = 0
  private var fpsValue = 0.0

  /** Current physical/numerical settings. */
  def simulationParameters: SimulationParameters = simulationValue

  /** Current geometry construction settings. */
  def geometryParameters: GeometryParameters = geometryParametersValue

  /** Current render-only settings. */
  def visualizationParameters: VisualizationParameters = visualizationValue

  /** Current concrete geometry generated from [[geometryParameters]]. */
  def geometry: PipeGeometry = geometryValue

  /** Current immutable particle state. */
  def state: SimulationState = stateValue

  /** Returns whether fixed-step physics is currently advancing. */
  def isRunning: Boolean = runningValue

  /** Latest approximately one-second rendering FPS estimate. */
  def fps: Double = fpsValue

  /** Calculates current aggregate flow diagnostics. */
  def metrics: FlowMetrics = metricsCalculator.calculate(stateValue, geometryValue, simulationValue)

  /** Starts or resumes physics updates. */
  def start(): Unit = runningValue = true

  /** Pauses physics while preserving the current frame and trails. */
  def pause(): Unit = runningValue = false

  /** Replaces live physical parameters without recreating particles. */
  def updateSimulationParameters(next: SimulationParameters): Unit = simulationValue = next

  /** Replaces render-only parameters and redraws the current state. */
  def updateVisualizationParameters(next: VisualizationParameters): Unit = {
    visualizationValue = next
    renderCurrent()
  }

  /** Rebuilds active geometry and resets particles so every particle fits the new volume. */
  def updateGeometryParameters(next: GeometryParameters): Unit = {
    geometryParametersValue = next
    geometryValue = geometryFactory.create(next)
    reset()
  }

  /** Recreates deterministic particle state and clears render-only history. */
  def reset(): Unit = {
    stateValue = engine.initialState(simulationValue, geometryValue)
    accumulator = 0.0
    renderer.clearTrails()
  }

  /** Advances fixed-step physics for one display-frame duration and renders the result. */
  def onFrame(frameSeconds: Double): Unit = {
    val safeFrame = math.max(0.0, math.min(frameSeconds, 0.25))
    updateFps(safeFrame)
    if (runningValue) {
      accumulator += safeFrame
      while (accumulator >= simulationValue.fixedTimeStep) {
        stateValue = engine.step(stateValue, simulationValue, geometryValue, simulationValue.fixedTimeStep)
        accumulator -= simulationValue.fixedTimeStep
      }
    }
    renderer.render(stateValue, geometryValue, simulationValue, visualizationValue)
  }

  /** Renders the current state without advancing physics. */
  def renderCurrent(): Unit = renderer.render(stateValue, geometryValue, simulationValue, visualizationValue)

  /** Updates the rolling FPS estimate approximately once per second. */
  private def updateFps(frameSeconds: Double): Unit = {
    fpsElapsed += frameSeconds
    fpsFrames += 1
    if (fpsElapsed >= 1.0) {
      fpsValue = fpsFrames / fpsElapsed
      fpsElapsed = 0.0
      fpsFrames = 0
    }
  }
}
