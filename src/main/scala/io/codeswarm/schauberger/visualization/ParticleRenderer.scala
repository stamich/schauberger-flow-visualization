package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.model.SimulationState
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color

/** Renders particles efficiently onto a single Canvas. */
final class ParticleRenderer(particleDiameter: Double = 3.2) {

  /** Draws every particle using the supplied world-to-screen transform. */
  def render(gc: GraphicsContext, state: SimulationState, transform: ViewportTransform): Unit = {
    gc.fill = Color.rgb(110, 205, 255, 0.88)
    val radius = particleDiameter / 2.0
    state.particles.foreach { particle =>
      val screen = transform.worldToScreen(particle.position)
      gc.fillOval(screen.x - radius, screen.y - radius, particleDiameter, particleDiameter)
    }
  }
}
