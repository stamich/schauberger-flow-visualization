package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.physics.secondary.{NoSecondaryFlow, SecondaryFlowModel}
import io.codeswarm.schauberger.physics.swirl.SwirlProfileFactory

/** Single assembly point for the force stack shared by the application, tests and benchmarks. */
object DefaultFlowForces {
  /** Builds the standard force stack: axial drive, one combined cross-section force, centripetal balance and wall.
   *
   * @param profileFactory  resolves the selected vortex profile
   * @param secondaryModel  geometry-induced secondary flow model
   * @param withSecondary   whether the secondary-flow model contributes; benchmarks drop it to measure the
   *                        cost of the remaining forces
   */
  def create(
              profileFactory: SwirlProfileFactory,
              secondaryModel: SecondaryFlowModel,
              withSecondary: Boolean = true
            ): CompositeFlowForce =
    CompositeFlowForce(Vector[FlowForce](
      new AxialFlowForce,
      new CrossSectionFlowForce(profileFactory, if (withSecondary) secondaryModel else NoSecondaryFlow),
      new CentripetalBalanceForce,
      new WallRepulsionForce
    ))
}
