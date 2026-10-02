package io.codeswarm.schauberger.geometry

/** Selects how an ovoid boundary is evaluated at runtime. */
sealed trait GeometryEvaluationMode

/** Supported ovoid geometry evaluation strategies. */
object GeometryEvaluationMode {
  /** Uses the exact analytic equations for every query. */
  case object Exact extends GeometryEvaluationMode

  /** Uses a precomputed boundary lookup table with linear interpolation. */
  case object Lookup extends GeometryEvaluationMode
}
