package io.codeswarm.schauberger.ui

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{SimulationParameters, SimulationState}
import io.codeswarm.schauberger.simulation.SimulationEngine
import io.codeswarm.schauberger.visualization.SimulationRenderer

/** Owns the interactive simulation lifecycle and fixed-timestep accumulator.
  *
  * This is the only layer that couples wall-clock frame timing to the otherwise
  * deterministic simulation engine.
  */
final class SimulationController(
    engine: SimulationEngine,
    geometry: PipeGeometry,
    renderer: SimulationRenderer,
    initialParameters: SimulationParameters
) {
  private var parametersValue: SimulationParameters = initialParameters
  private var stateValue: SimulationState = engine.initialState(parametersValue)
  private var runningValue: Boolean = true
  private var accumulator: Double = 0.0
  private var fpsValue: Double = 0.0
  private var fpsElapsed: Double = 0.0
  private var fpsFrames: Int = 0

  /** Current immutable parameter set. */
  def parameters: SimulationParameters = parametersValue

  /** Current immutable simulation state. */
  def state: SimulationState = stateValue

  /** Whether simulation updates are currently enabled. */
  def isRunning: Boolean = runningValue

  /** Most recently calculated rendering frames per second. */
  def fps: Double = fpsValue

  /** Starts or resumes simulation updates. */
  def start(): Unit = runningValue = true

  /** Pauses physics updates while allowing the last frame to remain visible. */
  def pause(): Unit = runningValue = false

  /** Replaces parameters without recreating existing particles. */
  def updateParameters(next: SimulationParameters): Unit = parametersValue = next

  /** Recreates particle state with current parameters and resets timing counters. */
  def reset(): Unit = {
    stateValue = engine.initialState(parametersValue)
    accumulator = 0.0
  }

  /** Advances the fixed-timestep accumulator and renders one display frame.
    *
    * @param frameSeconds wall-clock duration since the previous rendered frame
    */
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

    renderer.render(stateValue, geometry)
  }

  /** Renders the current state without advancing physics. */
  def renderCurrent(): Unit = renderer.render(stateValue, geometry)

  /** Updates the rolling one-second FPS estimate. */
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
