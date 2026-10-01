package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.SimulationState

import scala.collection.mutable

/** Mutable render-only ring-like history of recent particle positions.
  *
  * Mutation is intentionally confined to the visualization layer so the physical
  * simulation state remains immutable and deterministic.
  */
final class TrailBuffer {
  private val pointsByParticle = mutable.LongMap.empty[mutable.ArrayDeque[Vector3D]]
  private var lastRecordedFrame: Long = Long.MinValue

  /** Records current positions once per physics frame while retaining at most maxLength entries per particle. */
  def record(state: SimulationState, maxLength: Int): Unit = {
    if (maxLength <= 0) {
      clear()
    } else if (state.frame != lastRecordedFrame) {
      lastRecordedFrame = state.frame
      val currentIds = state.particles.iterator.map(_.id).toSet
      pointsByParticle.keys.filterNot(currentIds.contains).toVector.foreach(pointsByParticle.remove)
      state.particles.foreach { particle =>
        val deque = pointsByParticle.getOrElseUpdate(particle.id, mutable.ArrayDeque.empty[Vector3D])
        deque.append(particle.position)
        while (deque.length > maxLength) deque.removeHead()
      }
    }
  }

  /** Returns an immutable copy of one particle's recorded trail. */
  def points(particleId: Long): Vector[Vector3D] =
    pointsByParticle.get(particleId).map(_.toVector).getOrElse(Vector.empty)

  /** Removes all retained history, for example after reset or view discontinuity. */
  def clear(): Unit = {
    pointsByParticle.clear()
    lastRecordedFrame = Long.MinValue
  }
}
