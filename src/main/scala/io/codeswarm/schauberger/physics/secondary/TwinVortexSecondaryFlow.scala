package io.codeswarm.schauberger.physics.secondary

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.SecondaryFlowParameters

/** Simplified two-cell cross-sectional circulation driven by local pipe twist.
 *
 * This is an educational vector-field model, not a Navier-Stokes solution. The
 * field is derived from a smooth stream-function-like polynomial and attenuated
 * close to the actual cross-section boundary.
 */
final class TwinVortexSecondaryFlow(
                                     attenuation: BoundaryAttenuation = LinearBoundaryAttenuation
                                   ) extends SecondaryFlowModel {

  /** Calculates a finite target velocity in local cross-section coordinates. */
  override def targetVelocity(
                               localPosition: Vector2D,
                               geometry: PipeGeometry,
                               axialPosition: Double,
                               parameters: SecondaryFlowParameters
                             ): Vector2D = {
    if (!parameters.enabled || parameters.strength <= 0.0) return Vector2D.Zero

    val twist = math.abs(geometry.twistRateAt(axialPosition))
    if (twist <= Vector2D.Epsilon) return Vector2D.Zero

    val scale = math.max(geometry.characteristicRadius, Vector2D.Epsilon)
    val u = localPosition.x / scale
    val v = localPosition.y / scale
    val r2 = u * u + v * v
    val envelope = math.max(0.0, 1.0 - math.min(1.0, r2))

    // Stream-function-inspired two-cell field. The sign pattern creates
    // counter-rotating cross-sectional circulation while remaining smooth.
    val localU = -4.0 * u * v * envelope
    val localV = -envelope * (envelope - 4.0 * u * u)

    val twistFactor = math.min(1.0, twist * scale)
    val distance = geometry.crossSection.signedDistance(localPosition)
    val wallFactor = attenuation.factor(distance, parameters.boundaryFadeDistance)
    Vector2D(localU, localV) * (parameters.strength * twistFactor * wallFactor)
  }
}
