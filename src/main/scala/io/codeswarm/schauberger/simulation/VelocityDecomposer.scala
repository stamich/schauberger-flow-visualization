package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
import io.codeswarm.schauberger.model.Particle

/** Decomposes world-space particle velocity into local axial/cross-sectional components. */
final class VelocityDecomposer {
  /** Calculates axial, tangential, radial and local cross-section components. */
  def decompose(particle: Particle, geometry: PipeGeometry): VelocityComponents = {
    val frame = geometry.localFrameAt(particle.position)
    val tangent = frame.tangent.normalized
    val center = geometry.centerLinePosition(particle.position.x)
    val raw = particle.position - center
    val radialVector = raw - tangent * raw.dot(tangent)
    val radius = radialVector.magnitude
    val axial = particle.velocity.dot(tangent)
    val cross = frame.worldVectorToCrossSection(particle.velocity)
    if (radius <= Vector3D.Epsilon) VelocityComponents(axial, 0.0, 0.0, cross)
    else {
      val radialUnit = radialVector.normalized
      val tangentialUnit = tangent.cross(radialVector).normalized
      VelocityComponents(
        axial = axial,
        tangential = particle.velocity.dot(tangentialUnit),
        radial = particle.velocity.dot(radialUnit),
        crossSection = cross
      )
    }
  }
}
