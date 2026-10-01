package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}
import org.scalatest.funsuite.AnyFunSuite

/** Tests vector addition of independent force strategies. */
final class CompositeFlowForceSpec extends AnyFunSuite {
  test("sums force contributions") {
    val f1 = new FlowForce { override def acceleration(p: Particle, c: FlowContext): Vector3D = Vector3D(1, 2, 3) }
    val f2 = new FlowForce { override def acceleration(p: Particle, c: FlowContext): Vector3D = Vector3D(4, 5, 6) }
    val total = CompositeFlowForce(Vector(f1, f2)).acceleration(
      Particle(1, Vector3D.Zero, Vector3D.Zero),
      FlowContext(StraightCircularPipe(10, 1), SimulationParameters.Default)
    )
    assert(total == Vector3D(5, 7, 9))
  }
}
