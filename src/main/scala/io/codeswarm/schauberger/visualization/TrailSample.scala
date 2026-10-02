package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.Vector3D

/** One timestamped render-only trail sample. */
final case class TrailSample(position: Vector3D, simulationTime: Double)
