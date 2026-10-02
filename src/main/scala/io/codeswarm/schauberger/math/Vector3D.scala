package io.codeswarm.schauberger.math

/** Immutable three-dimensional vector used by the particle simulation. */
final case class Vector3D(x: Double, y: Double, z: Double) {
  /** Adds another vector component-wise. */
  def +(other: Vector3D): Vector3D = Vector3D(x + other.x, y + other.y, z + other.z)

  /** Subtracts another vector component-wise. */
  def -(other: Vector3D): Vector3D = Vector3D(x - other.x, y - other.y, z - other.z)

  /** Multiplies this vector by a scalar. */
  def *(scalar: Double): Vector3D = Vector3D(x * scalar, y * scalar, z * scalar)

  /** Divides this vector by a non-zero scalar. */
  def /(scalar: Double): Vector3D = {
    require(math.abs(scalar) > Vector3D.Epsilon)
    Vector3D(x / scalar, y / scalar, z / scalar)
  }

  /** Squared Euclidean magnitude. */
  def magnitudeSquared: Double = x * x + y * y + z * z

  /** Euclidean magnitude. */
  def magnitude: Double = math.sqrt(magnitudeSquared)

  /** Returns a unit vector or zero for a near-zero input. */
  def normalized: Vector3D = if (magnitude <= Vector3D.Epsilon) Vector3D.Zero else this / magnitude

  /** Dot product. */
  def dot(other: Vector3D): Double = x * other.x + y * other.y + z * other.z

  /** Right-handed cross product. */
  def cross(other: Vector3D): Vector3D = Vector3D(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)

  /** Caps magnitude while preserving direction. */
  def limit(max: Double): Vector3D = {
    require(max > 0.0)
    if (magnitude <= max) this else normalized * max
  }
}

/** Common constants for [[Vector3D]]. */
object Vector3D {
  val Epsilon: Double = 1e-9
  val Zero: Vector3D = Vector3D(0.0, 0.0, 0.0)
  val UnitX: Vector3D = Vector3D(1.0, 0.0, 0.0)
  val UnitY: Vector3D = Vector3D(0.0, 1.0, 0.0)
  val UnitZ: Vector3D = Vector3D(0.0, 0.0, 1.0)
}
