package io.codeswarm.schauberger.diagnostics

/** Estimates the axial component of cross-sectional vorticity by centered finite differences. */
final class VorticityFieldCalculator {
  /** Calculates `ωx = ∂vz/∂y - ∂vy/∂z` on the supplied regular grid. */
  def calculate(grid: VectorFieldGrid, gridStep: Double): ScalarFieldGrid = {
    require(gridStep > 0.0)
    val n = grid.resolution
    val cells = Vector.tabulate(n * n) { index =>
      val x = index % n
      val y = index / n
      val current = grid.cell(x, y)
      if (!current.inside || x == 0 || y == 0 || x == n - 1 || y == n - 1) {
        ScalarFieldCell(current.position, 0.0, current.inside)
      } else {
        val left = grid.cell(x - 1, y)
        val right = grid.cell(x + 1, y)
        val down = grid.cell(x, y - 1)
        val up = grid.cell(x, y + 1)
        if (!(left.inside && right.inside && down.inside && up.inside)) ScalarFieldCell(current.position, 0.0, inside = true)
        else {
          // local u maps to world/local normal (y-like), local v to binormal (z-like)
          val dvzDy = (right.velocity.z - left.velocity.z) / (2.0 * gridStep)
          val dvyDz = (up.velocity.y - down.velocity.y) / (2.0 * gridStep)
          ScalarFieldCell(current.position, dvzDy - dvyDz, inside = true)
        }
      }
    }
    ScalarFieldGrid(n, cells)
  }
}
