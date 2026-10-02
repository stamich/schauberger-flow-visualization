package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}

/** Geometry contract used by physics, generation, boundary handling and rendering. */
trait PipeGeometry {
  def length: Double

  def crossSection: CrossSectionShape

  /** Converts world coordinates to the unrotated local cross-section coordinates. */
  def toLocalCrossSection(position: Vector3D): Vector2D

  /** Converts local cross-section coordinates at x back to world coordinates. */
  def fromLocalCrossSection(x: Double, local: Vector2D): Vector3D

  /** Local pipe frame including any cross-section twist. */
  def localFrameAt(position: Vector3D): LocalFrame

  /** Center-line point at axial coordinate x. */
  def centerLinePosition(x: Double): Vector3D = Vector3D(x, 0.0, 0.0)

  /** Local tangent used by flow forces. */
  def tangentAt(position: Vector3D): Vector3D = localFrameAt(position).tangent

  /** Returns true for points inside axial bounds and the local cross-section. */
  def contains(position: Vector3D): Boolean = position.x >= 0.0 && position.x <= length && crossSection.contains(toLocalCrossSection(position))

  /** Positive inside distance to the local boundary. */
  def signedDistanceToBoundary(position: Vector3D): Double = crossSection.signedDistance(toLocalCrossSection(position))

  /** Inward unit normal transformed into world coordinates. */
  def inwardNormal(position: Vector3D): Vector3D = {
    val local = crossSection.inwardNormal(toLocalCrossSection(position))
    val frame = localFrameAt(position)
    (frame.normal * local.x + frame.binormal * local.y).normalized
  }

  /** Projects a point just inside the local cross-section. */
  def clampInside(position: Vector3D, epsilon: Double = 1e-6): Vector3D = fromLocalCrossSection(position.x, crossSection.clampInside(toLocalCrossSection(position), epsilon))

  /** Scale used by swirl and rendering. */
  def characteristicRadius: Double = crossSection.characteristicRadius

  /** Maximum local extent. */
  def boundingRadius: Double = crossSection.boundingRadius

  /** Twist rate in radians per axial simulation unit. */
  def twistRate: Double

  /** Returns local twist rate; future curved/tapered geometries may vary it with x. */
  def twistRateAt(x: Double): Double = twistRate

  /** Local cross-section rotation angle at x. */
  def rotationAngleAt(x: Double): Double
}
