package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{FlowMetrics, SimulationState}

/** Computes geometry-aware aggregate diagnostics without changing simulation state. */
final class FlowMetricsCalculator {
  def calculate(state: SimulationState, geometry: PipeGeometry): FlowMetrics = {
    if (state.particles.isEmpty) FlowMetrics.Zero
    else {
      var axial = 0.0; var tangential = 0.0; var angular = 0.0; var radialNorm = 0.0; var angularCount = 0L
      state.particles.foreach { p =>
        val tangent = geometry.tangentAt(p.position).normalized
        val center = geometry.centerLinePosition(p.position.x)
        val raw = p.position - center
        val radial = raw - tangent * raw.dot(tangent)
        val r = radial.magnitude
        axial += p.velocity.dot(tangent)
        radialNorm += math.max(0.0, math.min(1.0, geometry.crossSection.normalizedRadius(geometry.toLocalCrossSection(p.position))))
        if (r > Vector3D.Epsilon) {
          val t = tangent.cross(radial).normalized
          val vt = p.velocity.dot(t)
          tangential += vt; angular += vt / r; angularCount += 1
        }
      }
      val n = state.particles.size.toDouble
      val meanAngular = if (angularCount == 0) 0.0 else angular / angularCount.toDouble
      FlowMetrics(axial / n, tangential / n, meanAngular, 2.0 * meanAngular, radialNorm / n)
    }
  }
}
