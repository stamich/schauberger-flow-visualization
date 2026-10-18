package io.codeswarm.schauberger.physics.swirl

/** Computes target tangential speed as a function of radial distance.
 *
 * The speed is signed. A positive value turns in the configured rotation
 * direction and a negative value turns against it, so a profile may reverse
 * direction with radius. The simple profiles never return a negative value;
 * counter-rotating composites do. Consumers must therefore not assume
 * `speed >= 0`, and must take magnitudes where a magnitude is meant.
 */
trait SwirlProfile {
  /** Returns the signed target tangential speed at `radialDistance`. */
  def tangentialVelocity(
                          radialDistance: Double,
                          characteristicRadius: Double,
                          angularVelocity: Double
                        ): Double
}
