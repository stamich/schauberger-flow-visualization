package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector3D

/** Geometry contract shared by simulation, forces and visualization.
  *
  * The methods are intentionally expressed in 3D even though milestone 0.2 uses a
  * straight circular pipe. This makes later curved or twisted geometries possible
  * without changing the simulation engine.
  */
trait PipeGeometry {
  /** Total axial length of the simulated pipe. */
  def length: Double

  /** Nominal cross-section radius. */
  def radius: Double

  /** Returns true when a point lies inside both axial and radial bounds. */
  def contains(position: Vector3D): Boolean

  /** Returns distance from the local center line in the cross-section plane. */
  def radialDistance(position: Vector3D): Double

  /** Returns the center-line point corresponding to axial coordinate x. */
  def centerLinePosition(x: Double): Vector3D

  /** Returns the normalized local tangent of the pipe center line. */
  def tangentAt(position: Vector3D): Vector3D

  /** Clamps a point to the radial wall while preserving its axial coordinate. */
  def clampToWalls(position: Vector3D, epsilon: Double = 1e-6): Vector3D
}
