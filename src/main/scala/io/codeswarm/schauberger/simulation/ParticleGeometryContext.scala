package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.LocalFrame
import io.codeswarm.schauberger.math.{Vector2D, Vector3D}

/** Geometry data precomputed once for one particle and reused by flow models.
 *
 * @param center                 local pipe center-line point
 * @param localPosition          particle position in local cross-section coordinates
 * @param frame                  cached local frame including twist rotation
 * @param signedBoundaryDistance positive distance proxy inside the boundary
 * @param inwardNormal           inward world-space unit normal near the boundary
 * @param twistAngle             local cross-section twist angle in radians
 * @param twistRate              local twist rate in radians per axial simulation unit
 */
final case class ParticleGeometryContext(
                                          center: Vector3D,
                                          localPosition: Vector2D,
                                          frame: LocalFrame,
                                          signedBoundaryDistance: Double,
                                          inwardNormal: Vector3D,
                                          twistAngle: Double,
                                          twistRate: Double
                                        )
