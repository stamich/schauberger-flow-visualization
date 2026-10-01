package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.model.SimulationState
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color

/** Renders recent particle trajectories using the active 2D projection. */
final class TrailRenderer {
  /** Draws a polyline for each particle with at least two retained trail points. */
  def render(
      gc: GraphicsContext,
      state: SimulationState,
      trailBuffer: TrailBuffer,
      projection: Projection,
      transform: ViewportTransform
  ): Unit = {
    gc.stroke = Color.web("#78bdf2", 0.22)
    gc.lineWidth = 0.8
    state.particles.foreach { particle =>
      val trail = trailBuffer.points(particle.id)
      if (trail.size >= 2) {
        val first = transform.worldToScreen(projection.project(trail.head))
        gc.beginPath()
        gc.moveTo(first.x, first.y)
        trail.tail.foreach { point =>
          val screen = transform.worldToScreen(projection.project(point))
          gc.lineTo(screen.x, screen.y)
        }
        gc.stroke()
      }
    }
  }
}
