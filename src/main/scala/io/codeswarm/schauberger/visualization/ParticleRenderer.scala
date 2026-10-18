package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{SimulationState, SwirlParameters, ViewMode, VisualizationParameters, VortexRegion}
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color

/** Draws current tracer positions, filtering cross-section view to a physical axial slice.
 *
 * With sense colouring on, each tracer is coloured by the way it turns about the axis, so the layers of a
 * counter-rotating vortex are visible directly.
 */
final class ParticleRenderer(classifier: RotationSenseClassifier = new RotationSenseClassifier) {
  private val plain = Color.web(RotationSensePalette.Plain, 0.88)
  private val forward = Color.web(RotationSensePalette.Forward, 0.88)
  private val reverse = Color.web(RotationSensePalette.Reverse, 0.88)
  private val quiescent = Color.web(RotationSensePalette.Quiescent, 0.88)

  def render(gc: GraphicsContext, state: SimulationState, geometry: PipeGeometry, projection: Projection, transform: ViewportTransform, visualization: VisualizationParameters, sliceX: Double, swirl: SwirlParameters): Unit = {
    gc.fill = plain
    state.particles.iterator.filter { p => visualization.viewMode == ViewMode.Longitudinal || math.abs(p.position.x - sliceX) <= visualization.crossSectionSliceHalfWidth }.foreach { p =>
      if (visualization.colorBySense) {
        gc.fill = classifier.classify(p, geometry, swirl) match {
          case VortexRegion.Forward => forward
          case VortexRegion.Reverse => reverse
          case VortexRegion.Quiescent => quiescent
        }
      }
      val screen = transform.worldToScreen(projection.project(p.position))
      val depth = if (visualization.viewMode == ViewMode.Longitudinal) math.max(-1.0, math.min(1.0, p.position.z / geometry.boundingRadius)) else 0.0
      val diameter = 2.4 + 1.0 * (depth + 1.0) / 2.0
      gc.fillOval(screen.x - diameter / 2.0, screen.y - diameter / 2.0, diameter, diameter)
    }
  }
}
