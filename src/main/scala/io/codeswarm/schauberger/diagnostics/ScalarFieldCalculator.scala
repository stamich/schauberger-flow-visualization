package io.codeswarm.schauberger.diagnostics

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector2D

/** Converts an interpolated local velocity grid into a selected scalar diagnostic field. */
final class ScalarFieldCalculator(vorticityCalculator: VorticityFieldCalculator = new VorticityFieldCalculator) {
  /** Calculates one scalar field from the velocity grid and pipe geometry. */
  def calculate(fieldType: FieldType, grid: VectorFieldGrid, geometry: PipeGeometry): ScalarFieldGrid = {
    val step = (2.0 * geometry.boundingRadius) / (grid.resolution - 1).toDouble
    fieldType match {
      case FieldType.Vorticity => vorticityCalculator.calculate(grid, step)
      case _ =>
        val cells = grid.cells.map { cell =>
          if (!cell.inside) ScalarFieldCell(cell.position, 0.0, inside = false)
          else {
            val cross = Vector2D(cell.velocity.y, cell.velocity.z)
            val radius = cell.position.magnitude
            val tangential = if (radius <= 1e-12) 0.0 else {
              val tangentialUnit = Vector2D(-cell.position.y / radius, cell.position.x / radius)
              cross.dot(tangentialUnit)
            }
            val residualSecondary = math.sqrt(math.max(0.0, cross.magnitudeSquared - tangential * tangential))
            val value = fieldType match {
              case FieldType.VelocityMagnitude => cell.velocity.magnitude
              case FieldType.AxialVelocity => cell.velocity.x
              case FieldType.TangentialVelocity => tangential
              case FieldType.SecondaryVelocity => residualSecondary
              case FieldType.Vorticity => 0.0
            }
            ScalarFieldCell(cell.position, value, inside = true)
          }
        }
        ScalarFieldGrid(grid.resolution, cells)
    }
  }
}
