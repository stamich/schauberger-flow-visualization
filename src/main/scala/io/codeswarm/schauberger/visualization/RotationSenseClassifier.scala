package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, SwirlParameters, VortexRegion}
import io.codeswarm.schauberger.simulation.{DefaultGeometryContextCalculator, GeometryContextCalculator, VelocityDecomposer}

/** Tells which way one particle turns about the pipe axis, relative to the configured rotation direction.
 *
 * It uses the same rule as the aggregate region metrics, so the colours on the
 * canvas and the percentages in the status bar always agree.
 */
final class RotationSenseClassifier(
                                     geometryContextCalculator: GeometryContextCalculator = new DefaultGeometryContextCalculator,
                                     velocityDecomposer: VelocityDecomposer = new VelocityDecomposer
                                   ) {
  /** Classifies the particle as forward, reverse or quiescent. */
  def classify(particle: Particle, geometry: PipeGeometry, swirl: SwirlParameters): VortexRegion = {
    val context = geometryContextCalculator.calculate(particle, geometry)
    val radius = context.frame.crossSectionVectorToWorld(context.localPosition).magnitude
    if (radius <= Vector3D.Epsilon) VortexRegion.Quiescent
    else {
      val tangential = velocityDecomposer.decompose(particle, context).tangential
      VortexRegion.classify(swirl.rotationDirection.sign * tangential / radius, swirl.angularVelocity)
    }
  }
}
