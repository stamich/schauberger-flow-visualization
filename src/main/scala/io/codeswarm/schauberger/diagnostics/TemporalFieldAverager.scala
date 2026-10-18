package io.codeswarm.schauberger.diagnostics

/** Exponential moving average of successive scalar grids.
 *
 * Particle-interpolated fields flicker from frame to frame. Averaging them
 * makes slow structure, such as a stable zero crossing, visible. The average
 * restarts whenever the key (field, source, resolution, ...) or the grid layout
 * changes, so values of different quantities are never mixed.
 */
final class TemporalFieldAverager {
  private var currentKey: Option[Any] = None
  private var previous: Option[ScalarFieldGrid] = None

  /** Folds `current` into the average and returns the grid to draw.
   *
   * @param key     identifies what is being averaged; a different key restarts the average
   * @param memory  weight of the earlier average in `[0, 1)`; zero returns `current` unchanged
   */
  def update(key: Any, current: ScalarFieldGrid, memory: Double): ScalarFieldGrid = {
    require(memory >= 0.0 && memory < 1.0)
    val result = previous match {
      case Some(old) if currentKey.contains(key) && sameLayout(old, current) && memory > 0.0 =>
        val cells = current.cells.zip(old.cells).map { case (now, before) =>
          if (now.inside && now.value.isFinite && before.value.isFinite)
            now.copy(value = memory * before.value + (1.0 - memory) * now.value)
          else now
        }
        ScalarFieldGrid(current.resolution, cells)
      case _ => current
    }
    currentKey = Some(key)
    previous = Some(result)
    result
  }

  /** Forgets the running average. */
  def reset(): Unit = {
    currentKey = None
    previous = None
  }

  private def sameLayout(a: ScalarFieldGrid, b: ScalarFieldGrid): Boolean =
    a.resolution == b.resolution && a.cells.indices.forall(i => a.cells(i).inside == b.cells(i).inside)
}
