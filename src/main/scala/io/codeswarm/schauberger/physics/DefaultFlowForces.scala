package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.physics.secondary.SecondaryFlowModel
import io.codeswarm.schauberger.physics.swirl.SwirlProfileFactory

/** Single assembly point for the force stack shared by the application, tests and benchmarks. */
object DefaultFlowForces {
  /** Builds the standard force stack.
   *
   * @param profileFactory  resolves the selected vortex profile
   * @param secondaryModel  geometry-induced secondary flow model
   * @param withSecondary   whether the secondary-flow force is part of the stack; benchmarks drop it to
   *                        measure the cost of the remaining forces
   */
  def create(
              profileFactory: SwirlProfileFactory,
              secondaryModel: SecondaryFlowModel,
              withSecondary: Boolean = true
            ): CompositeFlowForce = {
    val primary = Vector[FlowForce](new AxialFlowForce, new SwirlForce(profileFactory))
    val secondary = if (withSecondary) Vector[FlowForce](new SecondaryFlowForce(secondaryModel)) else Vector.empty[FlowForce]
    CompositeFlowForce(primary ++ secondary ++ Vector[FlowForce](new CentripetalBalanceForce, new WallRepulsionForce))
  }
}
