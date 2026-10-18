package io.codeswarm.schauberger.model

/** Parameters of the centripetal balance that keeps swirling tracers on their orbit.
 *
 * In a real vortex a radial pressure gradient `dp/dr = rho * v_theta^2 / r`
 * supplies the centripetal acceleration. The particle model has no pressure
 * field, so [[io.codeswarm.schauberger.physics.CentripetalBalanceForce]] applies
 * the equivalent acceleration directly.
 *
 * @param enabled  whether the balance acceleration is applied
 * @param strength multiplier of the ideal balance; 1.0 holds a circular orbit exactly
 */
final case class RadialBalanceParameters(enabled: Boolean, strength: Double) {
  require(strength >= 0.0)
}

/** Default radial-balance settings. */
object RadialBalanceParameters {
  val Default: RadialBalanceParameters = RadialBalanceParameters(enabled = true, strength = 1.0)
}
