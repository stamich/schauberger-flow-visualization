package io.codeswarm.schauberger.ui

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{FlowMetrics, SimulationParameters, SimulationState, ViewMode}
import io.codeswarm.schauberger.simulation.{FlowMetricsCalculator, SimulationEngine}
import io.codeswarm.schauberger.visualization.SimulationRenderer

/** Coordinates UI commands, fixed-timestep simulation updates and rendering. */
final class SimulationController(
    engine: SimulationEngine,
    geometry: PipeGeometry,
    renderer: SimulationRenderer,
    metricsCalculator: FlowMetricsCalculator,
    initialParameters: SimulationParameters
) {
  private var parametersValue = initialParameters
  private var stateValue = engine.initialState(initialParameters)
  private var runningValue = true
  private var viewModeValue: ViewMode = ViewMode.Longitudinal
  private var accumulator = 0.0
  private var fpsElapsed = 0.0
  private var fpsFrames = 0
  private var fpsValue = 0.0

  /** Current immutable parameter set. */
  def parameters: SimulationParameters = parametersValue

  /** Current immutable physical state. */
  def state: SimulationState = stateValue

  /** Current selected visualization projection. */
  def viewMode: ViewMode = viewModeValue

  /** Whether physics updates are enabled. */
  def isRunning: Boolean = runningValue

  /** Most recently calculated rendering frames per second. */
  def fps: Double = fpsValue

  /** Calculates current aggregate flow diagnostics. */
  def metrics: FlowMetrics = metricsCalculator.calculate(stateValue, geometry)

  /** Starts or resumes simulation updates. */
  def start(): Unit = runningValue = true

  /** Pauses physics updates while retaining the rendered frame. */
  def pause(): Unit = runningValue = false

  /** Replaces user-adjustable parameters without recreating particles. */
  def updateParameters(next: SimulationParameters): Unit = parametersValue = next

  /** Changes the active two-dimensional view. */
  def setViewMode(next: ViewMode): Unit = viewModeValue = next

  /** Recreates particle state and clears accumulated visual trails. */
  def reset(): Unit = {
    stateValue = engine.initialState(parametersValue)
    accumulator = 0.0
    renderer.clearTrails()
  }

  /** Advances fixed-step physics according to one display-frame duration and renders. */
  def onFrame(frameSeconds: Double): Unit = {
    val safeFrame = math.max(0.0, math.min(frameSeconds, 0.25))
    updateFps(safeFrame)
    if (runningValue) {
      accumulator += safeFrame
      while (accumulator >= parametersValue.fixedTimeStep) {
        stateValue = engine.step(stateValue, parametersValue, parametersValue.fixedTimeStep)
        accumulator -= parametersValue.fixedTimeStep
      }
    }
    renderer.render(stateValue, geometry, parametersValue, viewModeValue)
  }

  /** Renders the current state without advancing physics. */
  def renderCurrent(): Unit = renderer.render(stateValue, geometry, parametersValue, viewModeValue)

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
