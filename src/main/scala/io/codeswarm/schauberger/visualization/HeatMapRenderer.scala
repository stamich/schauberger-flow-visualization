package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.diagnostics.{CrossSectionVelocityFieldSampler, ScalarFieldCalculator}
import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{SimulationState, VisualizationParameters}
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color

/** Renders interpolated scalar flow diagnostics as a cross-section heat map. */
final class HeatMapRenderer(
    velocitySampler: CrossSectionVelocityFieldSampler,
    scalarCalculator: ScalarFieldCalculator
) {
  /** Samples the selected field and paints colored grid cells beneath particles. */
  def render(
      gc: GraphicsContext,
      state: SimulationState,
      geometry: PipeGeometry,
      transform: ViewportTransform,
      axialPosition: Double,
      visualization: VisualizationParameters
  ): Unit = {
    if (!visualization.showHeatMap) return
    val grid = velocitySampler.sample(
      state,
      geometry,
      axialPosition,
      visualization.heatMapResolution,
      visualization.crossSectionSliceHalfWidth
    )
    val scalar = scalarCalculator.calculate(visualization.heatMapField, grid, geometry)
    val range = scalar.range
    val radius = geometry.boundingRadius
    val localStep = (2.0 * radius) / (visualization.heatMapResolution - 1).toDouble
    val pixelStep = transform.canvasWidth / (2.0 * radius) * localStep

    scalar.cells.foreach { cell =>
      if (cell.inside && cell.value.isFinite) {
        val world = geometry.fromLocalCrossSection(axialPosition, cell.position)
        val screen = transform.worldToScreen(io.codeswarm.schauberger.math.Vector2D(world.y, world.z))
        gc.fill = heatColor(range.normalize(cell.value))
        gc.fillRect(screen.x - pixelStep / 2.0, screen.y - pixelStep / 2.0, pixelStep + 1.0, pixelStep + 1.0)
      }
    }
    drawLegend(gc, visualization.heatMapField.displayName, range.min, range.max, transform)
  }

  /** Maps a normalized scalar value to a perceptually ordered blue-red ramp. */
  private def heatColor(value: Double): Color = {
    val v = math.max(0.0, math.min(1.0, value))
    val hue = 240.0 * (1.0 - v)
    Color.hsb(hue, 0.85, 0.95, 0.48)
  }

  /** Draws a compact textual minimum/maximum legend. */
  private def drawLegend(gc: GraphicsContext, name: String, min: Double, max: Double, transform: ViewportTransform): Unit = {
    gc.fill = Color.web("#e2e8f0", 0.9)
    gc.fillText(f"$name: $min%.2f .. $max%.2f", 12.0, transform.canvasHeight - 14.0)
  }
}
