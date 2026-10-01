package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{FlowMetrics, SimulationState}

/** Computes aggregate diagnostics without influencing the simulation state. */
final class FlowMetricsCalculator {

  /** Calculates signed axial, tangential and angular velocity statistics. */
  def calculate(state: SimulationState, geometry: PipeGeometry): FlowMetrics = {
    if (state.particles.isEmpty) FlowMetrics.Zero
    else {
      var axialSum = 0.0
      var tangentialSum = 0.0
      var angularSum = 0.0
      var angularCount = 0L

      state.particles.foreach { particle =>
        val tangent = geometry.tangentAt(particle.position).normalized
        val center = geometry.centerLinePosition(particle.position.x)
        val rawRadial = particle.position - center
        val radial = rawRadial - tangent * rawRadial.dot(tangent)
        val radius = radial.magnitude
        axialSum += particle.velocity.dot(tangent)
        if (radius > Vector3D.Epsilon) {
          val positiveTangential = tangent.cross(radial).normalized
          val tangential = particle.velocity.dot(positiveTangential)
          tangentialSum += tangential
          angularSum += tangential / radius
          angularCount += 1L
        }
      }

      val n = state.particles.size.toDouble
      val meanAngular = if (angularCount == 0L) 0.0 else angularSum / angularCount.toDouble
      FlowMetrics(
        meanAxialVelocity = axialSum / n,
        meanTangentialVelocity = tangentialSum / n,
        meanAngularVelocity = meanAngular,
        vorticityProxy = 2.0 * meanAngular
      )
    }
  }
}
