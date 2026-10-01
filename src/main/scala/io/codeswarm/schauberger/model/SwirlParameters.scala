package io.codeswarm.schauberger.model

/** Parameters controlling tangential swirl around the local pipe tangent. */
final case class SwirlParameters(
    angularVelocity: Double,
    response: Double,
    rotationDirection: RotationDirection
) {
  require(angularVelocity >= 0.0)
  require(response >= 0.0)
}

/** Default swirl settings tuned for readable trajectories. */
object SwirlParameters {
  val Default: SwirlParameters = SwirlParameters(
    angularVelocity = 0.55,
    response = 2.0,
    rotationDirection = RotationDirection.CounterClockwise
  )
}
