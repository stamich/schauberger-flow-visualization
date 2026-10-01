package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle

/** Decomposes world-space velocity using a precomputed local geometry frame. */
final class VelocityDecomposer {
  /** Calculates axial, tangential, radial and local cross-section components. */
  def decompose(particle: Particle, geometry: ParticleGeometryContext): VelocityComponents = {
    val tangent = geometry.frame.tangent.normalized
    val radialVector = geometry.frame.crossSectionVectorToWorld(geometry.localPosition)
    val radius = radialVector.magnitude
    val axial = particle.velocity.dot(tangent)
    val cross = geometry.frame.worldVectorToCrossSection(particle.velocity)
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
