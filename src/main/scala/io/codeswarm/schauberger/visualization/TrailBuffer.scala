package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.model.SimulationState
import scala.collection.mutable

/** Bounded render-only trail history with both length and age-based expiration. */
final class TrailBuffer {
  private val samples = mutable.LongMap.empty[mutable.ArrayDeque[TrailSample]]
  private var lastRecordedFrame = Long.MinValue

  /** Records at a reduced cadence, prunes old samples and caps per-particle history. */
  def record(state: SimulationState, maxLength: Int, maxAgeSeconds: Double, everyFrames: Int): Unit = {
    if (maxLength <= 0 || maxAgeSeconds <= 0.0) clear()
    else if (state.frame != lastRecordedFrame && state.frame % everyFrames == 0L) {
      lastRecordedFrame = state.frame
      val minTime = state.elapsedTime - maxAgeSeconds
      val currentIds = state.particles.iterator.map(_.id).toSet
      samples.keys.filterNot(currentIds.contains).toVector.foreach(samples.remove)
      state.particles.foreach { p =>
        val deque = samples.getOrElseUpdate(p.id, mutable.ArrayDeque.empty[TrailSample])
        deque.append(TrailSample(p.position, state.elapsedTime))
        while (deque.nonEmpty && (deque.head.simulationTime < minTime || deque.length > maxLength)) deque.removeHead()
      }
    }
  }
  /** Immutable snapshot for one particle. */
  def points(particleId: Long): Vector[TrailSample] = samples.get(particleId).map(_.toVector).getOrElse(Vector.empty)
  /** Clears all visual history. */
  def clear(): Unit = { samples.clear(); lastRecordedFrame = Long.MinValue }
}
