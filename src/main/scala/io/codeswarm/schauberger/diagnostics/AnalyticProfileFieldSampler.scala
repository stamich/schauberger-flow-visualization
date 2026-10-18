package io.codeswarm.schauberger.diagnostics

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
import io.codeswarm.schauberger.model.SwirlParameters
import io.codeswarm.schauberger.physics.swirl.SwirlProfileFactory

/** Evaluates the selected swirl profile directly on a cross-section grid.
 *
 * With 750 particles the interpolated particle field is noisy, and a
 * counter-rotating vortex is easy to mistake for noise. This grid has no
 * particles in it: every inside cell holds the exact profile velocity, tangent
 * to circles about the axis and signed by the rotation direction, with the
 * same encoding as [[CrossSectionVelocityFieldSampler]] (axial, then the two
 * in-plane components). The axial component is zero because the profile
 * describes swirl only.
 */
final class AnalyticProfileFieldSampler(profileFactory: SwirlProfileFactory) {
  /** Samples the profile on a `resolution x resolution` grid covering the bounding radius. */
  def sample(geometry: PipeGeometry, swirl: SwirlParameters, resolution: Int): VectorFieldGrid = {
    require(resolution >= 3)
    val radius = geometry.boundingRadius
    val step = (2.0 * radius) / (resolution - 1).toDouble
    val profile = profileFactory.create(swirl.profile)
    val cells = Vector.tabulate(resolution * resolution) { index =>
      val position = Vector2D(-radius + (index % resolution) * step, -radius + (index / resolution) * step)
      if (!geometry.crossSection.contains(position)) VectorFieldCell(position, Vector3D.Zero, inside = false)
      else {
        val distance = position.magnitude
        if (distance <= 1e-12) VectorFieldCell(position, Vector3D.Zero, inside = true)
        else {
          val speed = profile.tangentialVelocity(distance, geometry.characteristicRadius, swirl.angularVelocity) * swirl.rotationDirection.sign
          VectorFieldCell(position, Vector3D(0.0, -position.y / distance * speed, position.x / distance * speed), inside = true)
        }
      }
    }
    VectorFieldGrid(resolution, cells)
  }
}
