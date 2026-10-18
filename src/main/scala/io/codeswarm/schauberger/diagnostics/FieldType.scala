package io.codeswarm.schauberger.diagnostics

/** Scalar quantity that can be rendered as a cross-section heat map. */
sealed trait FieldType {
  /** Name shown in the UI. */
  def displayName: String

  /** True when the sign carries meaning (rotation sense), so the field is drawn on a range centered on zero. */
  def signed: Boolean = false
}

/** Supported diagnostic fields. */
object FieldType {
  case object VelocityMagnitude extends FieldType { val displayName = "Velocity magnitude" }
  case object AxialVelocity extends FieldType { val displayName = "Axial velocity" }
  case object TangentialVelocity extends FieldType {
    val displayName = "Tangential velocity"
    override val signed: Boolean = true
  }
  case object SecondaryVelocity extends FieldType { val displayName = "Secondary velocity" }
  case object Vorticity extends FieldType {
    val displayName = "Vorticity"
    override val signed: Boolean = true
  }

  /** Stable order used by the ScalaFX combo box. */
  val values: Vector[FieldType] = Vector(VelocityMagnitude, AxialVelocity, TangentialVelocity, SecondaryVelocity, Vorticity)

  /** Resolves a UI display value, defaulting to velocity magnitude. */
  def fromDisplayName(name: String): FieldType = values.find(_.displayName == name).getOrElse(VelocityMagnitude)
}
