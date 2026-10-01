package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector2D

/** Geometry contract used by the simulation and rendering layers.
  *
  * Milestone 0.1 uses a longitudinal 2D section of a straight cylindrical pipe.
  * The abstraction exists so later milestones can introduce ovoid and twisted
  * geometries without coupling them to the simulation engine.
  */
trait PipeGeometry {

  /** Length of the simulated pipe along its primary x axis. */
  def length: Double

  /** Half-height of the current longitudinal pipe section. */
  def radius: Double

  /** Returns true when a point is within the longitudinal simulation domain. */
  def contains(position: Vector2D): Boolean

  /** Returns the absolute distance of a point from the pipe center line. */
  def distanceFromCenter(position: Vector2D): Double

  /** Clamps the vertical coordinate to the physical pipe walls. */
  def clampToWalls(position: Vector2D): Vector2D
}
