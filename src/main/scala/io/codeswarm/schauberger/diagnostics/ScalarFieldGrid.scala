package io.codeswarm.schauberger.diagnostics

/** Regular cross-section scalar grid used by heat-map rendering. */
final case class ScalarFieldGrid(resolution: Int, cells: Vector[ScalarFieldCell]) {
  require(resolution >= 3)
  require(cells.size == resolution * resolution)

  /** Finite values belonging to cells inside the active cross-section. */
  private def insideValues: Vector[Double] = cells.collect { case c if c.inside && c.value.isFinite => c.value }

  /** Returns minimum and maximum finite inside values, or zeros for an empty grid. */
  def range: FieldRange = FieldRange.from(insideValues)
}
