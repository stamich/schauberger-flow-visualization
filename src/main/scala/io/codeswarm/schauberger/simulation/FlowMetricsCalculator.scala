package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{FlowMetrics, SimulationParameters, SimulationState}
import io.codeswarm.schauberger.physics.secondary.SecondaryFlowModel

/** Computes geometry-aware aggregate diagnostics without changing simulation state. */
final class FlowMetricsCalculator(
                                   secondaryFlowModel: SecondaryFlowModel,
                                   velocityDecomposer: VelocityDecomposer = new VelocityDecomposer,
                                   geometryContextCalculator: GeometryContextCalculator = new DefaultGeometryContextCalculator
                                 ) {
  /** Calculates axial, swirl, occupancy and secondary-flow diagnostics. */
  def calculate(state: SimulationState, geometry: PipeGeometry, parameters: SimulationParameters): FlowMetrics = {
    if (state.particles.isEmpty) FlowMetrics.Zero
    else {
      var axial = 0.0
      var tangential = 0.0
      var angular = 0.0
      var radialNorm = 0.0
      var angularCount = 0L
      var secondarySpeed = 0.0
      var secondaryEnergy = 0.0
      var totalEnergy = 0.0

      state.particles.foreach { particle =>
        val g = geometryContextCalculator.calculate(particle, geometry)
        val radial = g.frame.crossSectionVectorToWorld(g.localPosition)
        val radius = radial.magnitude
        val components = velocityDecomposer.decompose(particle, g)
        axial += components.axial
        radialNorm += math.max(0.0, math.min(1.0, geometry.crossSection.normalizedRadius(g.localPosition)))
        if (radius > Vector3D.Epsilon) {
          tangential += components.tangential
          angular += components.tangential / radius
          angularCount += 1
        }
        val targetSecondary = secondaryFlowModel.targetVelocity(g.localPosition, geometry, particle.position.x, parameters.secondaryFlow)
        val speed = targetSecondary.magnitude
        secondarySpeed += speed
        secondaryEnergy += speed * speed
        totalEnergy += particle.velocity.magnitudeSquared
      }

      val n = state.particles.size.toDouble
      val meanAngular = if (angularCount == 0) 0.0 else angular / angularCount.toDouble
      val ratio = if (totalEnergy <= Vector3D.Epsilon) 0.0 else secondaryEnergy / totalEnergy
      FlowMetrics(
        meanAxialVelocity = axial / n,
        meanTangentialVelocity = tangential / n,
        meanAngularVelocity = meanAngular,
        vorticityProxy = 2.0 * meanAngular,
        meanNormalizedRadialPosition = radialNorm / n,
        meanSecondaryVelocity = secondarySpeed / n,
        secondaryFlowEnergyRatio = ratio
      )
    }
  }
}
