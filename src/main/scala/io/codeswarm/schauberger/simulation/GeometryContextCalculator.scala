package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.Particle

/** Calculates reusable geometry data for one particle update. */
trait GeometryContextCalculator {
  /** Builds the context required by force and diagnostic calculations. */
  def calculate(particle: Particle, geometry: PipeGeometry): ParticleGeometryContext
}

/** Default geometry-context implementation. */
final class DefaultGeometryContextCalculator extends GeometryContextCalculator {
  /** Computes local coordinates, frame, boundary information and twist exactly once. */
  override def calculate(particle: Particle, geometry: PipeGeometry): ParticleGeometryContext = {
    val x = particle.position.x
    val frame = geometry.localFrameAt(particle.position)
    val local = frame.worldPointToLocal(particle.position)
    val boundary = geometry.crossSection.boundaryQuery(local)
    val distance = boundary.signedDistance
    val worldNormal = frame.crossSectionVectorToWorld(boundary.inwardNormal).normalized
    ParticleGeometryContext(
      center = frame.origin,
      localPosition = local,
      frame = frame,
      signedBoundaryDistance = distance,
      inwardNormal = worldNormal,
      twistAngle = frame.rotationAngle,
      twistRate = geometry.twistRateAt(x)
    )
  }
}
