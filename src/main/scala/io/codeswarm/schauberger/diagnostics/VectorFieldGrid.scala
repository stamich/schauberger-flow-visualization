package io.codeswarm.schauberger.diagnostics

/** Regular cross-section vector grid stored in row-major order. */
final case class VectorFieldGrid(resolution: Int, cells: Vector[VectorFieldCell]) {
  require(resolution >= 3)
  require(cells.size == resolution * resolution)

  /** Returns a cell by integer grid coordinates. */
  def cell(x: Int, y: Int): VectorFieldCell = cells(y * resolution + x)
}
