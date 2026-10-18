package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{FlowMetrics, RadialOccupancy, SimulationParameters, SimulationState, VortexRegion, VortexRegionMetrics}
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
      val thirds = Array(0L, 0L, 0L)
      var forwardCount = 0L
      var reverseCount = 0L
      var quiescentCount = 0L
      var forwardSum = 0.0
      var reverseSum = 0.0
      var absoluteSum = 0.0
      val direction = parameters.swirl.rotationDirection.sign

      state.particles.foreach { particle =>
        val g = geometryContextCalculator.calculate(particle, geometry)
        val radial = g.frame.crossSectionVectorToWorld(g.localPosition)
        val radius = radial.magnitude
        val components = velocityDecomposer.decompose(particle, g)
        axial += components.axial
        val normalizedRadius = math.max(0.0, math.min(1.0, geometry.crossSection.normalizedRadius(g.localPosition)))
        radialNorm += normalizedRadius
        thirds(RadialOccupancy.thirdOf(normalizedRadius)) += 1L
        if (radius > Vector3D.Epsilon) {
          tangential += components.tangential
          angular += components.tangential / radius
          angularCount += 1
        }
        val signedAngular = if (radius > Vector3D.Epsilon) direction * components.tangential / radius else 0.0
        VortexRegion.classify(signedAngular, parameters.swirl.angularVelocity) match {
          case VortexRegion.Forward => forwardCount += 1L; forwardSum += signedAngular
          case VortexRegion.Reverse => reverseCount += 1L; reverseSum += signedAngular
          case VortexRegion.Quiescent => quiescentCount += 1L
        }
        absoluteSum += math.abs(signedAngular)
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
        secondaryFlowEnergyRatio = ratio,
        occupancy = RadialOccupancy.fromCounts(thirds(0), thirds(1), thirds(2)),
        regions = VortexRegionMetrics.fromAccumulators(forwardCount, reverseCount, quiescentCount, forwardSum, reverseSum, absoluteSum)
      )
    }
  }
}
