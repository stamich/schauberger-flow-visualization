package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{SimulationState, SwirlParameters, ViewMode, VisualizationParameters}
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color

/** Renders short fading trajectory segments instead of indefinitely accumulating lines.
 *
 * With sense colouring on, a trail takes the colour of the way its particle currently turns.
 */
final class TrailRenderer(classifier: RotationSenseClassifier = new RotationSenseClassifier) {
  def render(gc: GraphicsContext, state: SimulationState, trailBuffer: TrailBuffer, projection: Projection, transform: ViewportTransform, visualization: VisualizationParameters, sliceX: Double, geometry: PipeGeometry, swirl: SwirlParameters): Unit = {
    gc.lineWidth = 0.8
    state.particles.foreach { particle =>
      val hex = if (visualization.colorBySense) RotationSensePalette.hex(classifier.classify(particle, geometry, swirl)) else "#78bdf2"
      val raw = trailBuffer.points(particle.id)
      val trail = visualization.viewMode match {
        case ViewMode.Longitudinal => raw
        case ViewMode.CrossSection => raw.filter(s => math.abs(s.position.x - sliceX) <= visualization.crossSectionSliceHalfWidth)
      }
      trail.sliding(2).foreach {
        case Vector(a, b) =>
          val age = math.max(0.0, state.elapsedTime - b.simulationTime)
          val fade = if (visualization.trailDurationSeconds <= 0.0) 0.0 else math.max(0.0, 1.0 - age / visualization.trailDurationSeconds)
          if (fade > 0.01) {
            val pa = transform.worldToScreen(projection.project(a.position))
            val pb = transform.worldToScreen(projection.project(b.position))
            gc.stroke = Color.web(hex, 0.30 * fade)
            gc.strokeLine(pa.x, pa.y, pb.x, pb.y)
          }
        case _ => ()
      }
    }
  }
}
