package io.codeswarm.schauberger.benchmark

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.model.SimulationParameters
import io.codeswarm.schauberger.physics.{AxialFlowForce, CompositeFlowForce, WallRepulsionForce}
import io.codeswarm.schauberger.simulation.{PipeBoundaryHandler, SemiImplicitEulerIntegrator, SimulationEngine, UniformInletParticleGenerator}

/** Lightweight headless performance smoke benchmark for the simulation kernel.
  *
  * This intentionally avoids rendering and is not a replacement for JMH. It gives
  * milestone-to-milestone feedback before a dedicated benchmark suite is introduced.
  */
object SimulationBenchmark {

  /** Runs warmup and measured workloads for several particle counts. */
  def main(args: Array[String]): Unit = {
    val counts = Vector(1000, 10000, 50000)
    println("particles,steps,elapsed_ms,updates_per_second,particle_updates_per_second")
    counts.foreach(runCase)
  }

  /** Runs one deterministic benchmark case and prints CSV-compatible output. */
  private def runCase(count: Int): Unit = {
    val geometry = StraightCircularPipe(1000.0, 120.0)
    val forceModel = CompositeFlowForce(Vector(new AxialFlowForce, new WallRepulsionForce))
    val engine = new SimulationEngine(
      geometry,
      forceModel,
      new SemiImplicitEulerIntegrator,
      new UniformInletParticleGenerator(42L),
      new PipeBoundaryHandler
    )
    val params = SimulationParameters.Default.copy(particleCount = count)
    val dt = params.fixedTimeStep

    var warm = engine.initialState(params)
    var i = 0
    while (i < 120) {
      warm = engine.step(warm, params, dt)
      i += 1
    }

    val measuredSteps = if (count >= 50000) 240 else 600
    var state = warm
    val started = System.nanoTime()
    var step = 0
    while (step < measuredSteps) {
      state = engine.step(state, params, dt)
      step += 1
    }
    val elapsedNanos = System.nanoTime() - started
    val elapsedSeconds = elapsedNanos.toDouble / 1_000_000_000.0
    val updatesPerSecond = measuredSteps / elapsedSeconds
    val particleUpdatesPerSecond = measuredSteps.toDouble * count / elapsedSeconds
    println(f"$count,$measuredSteps,${elapsedSeconds * 1000.0}%.2f,$updatesPerSecond%.2f,$particleUpdatesPerSecond%.2f")

    // Prevent over-aggressive elimination in future optimized benchmark variants.
    require(state.frame > 0L)
  }
}
