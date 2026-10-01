package io.codeswarm.schauberger.benchmark

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.model.SimulationParameters
import io.codeswarm.schauberger.physics.{AxialFlowForce, CompositeFlowForce, SolidBodySwirlProfile, SwirlForce, WallRepulsionForce}
import io.codeswarm.schauberger.simulation.{PipeBoundaryHandler, SemiImplicitEulerIntegrator, SimulationEngine, UniformInletParticleGenerator}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

/** Lightweight headless milestone benchmark with axial-only and swirl scenarios.
  *
  * This is deliberately a regression/smoke benchmark rather than a replacement for JMH.
  * Results are printed as CSV and saved as JSON for milestone-to-milestone comparison.
  */
object SimulationBenchmark {
  /** One measured benchmark result row. */
  private final case class Result(
      scenario: String,
      particles: Int,
      steps: Int,
      elapsedMs: Double,
      updatesPerSecond: Double,
      particleUpdatesPerSecond: Double
  )

  /** Runs both scenarios for three population sizes and writes benchmark/results/benchmark-0.2.0.json. */
  def main(args: Array[String]): Unit = {
    val counts = Vector(1000, 10000, 50000)
    val scenarios = Vector("axial-only", "axial-plus-swirl")
    println("scenario,particles,steps,elapsed_ms,updates_per_second,particle_updates_per_second")
    val results = for {
      scenario <- scenarios
      count <- counts
    } yield runCase(scenario, count)
    writeJson(results, Paths.get("benchmark/results/benchmark-0.2.0.json"))
  }

  /** Executes one deterministic warmup and measured benchmark case. */
  private def runCase(scenario: String, count: Int): Result = {
    val geometry = StraightCircularPipe(1000.0, 120.0)
    val forces = scenario match {
      case "axial-only" => Vector(new AxialFlowForce, new WallRepulsionForce)
      case "axial-plus-swirl" => Vector(new AxialFlowForce, new SwirlForce(new SolidBodySwirlProfile), new WallRepulsionForce)
      case other => throw new IllegalArgumentException(s"Unknown benchmark scenario: $other")
    }
    val engine = new SimulationEngine(
      geometry,
      CompositeFlowForce(forces),
      new SemiImplicitEulerIntegrator,
      new UniformInletParticleGenerator(42L),
      new PipeBoundaryHandler
    )
    val params = SimulationParameters.Default.copy(particleCount = count)
    val dt = params.fixedTimeStep
    var state = engine.initialState(params)
    var warm = 0
    while (warm < 120) {
      state = engine.step(state, params, dt)
      warm += 1
    }
    val measuredSteps = if (count >= 50000) 240 else 600
    val started = System.nanoTime()
    var step = 0
    while (step < measuredSteps) {
      state = engine.step(state, params, dt)
      step += 1
    }
    val elapsedSeconds = (System.nanoTime() - started).toDouble / 1_000_000_000.0
    val result = Result(
      scenario,
      count,
      measuredSteps,
      elapsedSeconds * 1000.0,
      measuredSteps / elapsedSeconds,
      measuredSteps.toDouble * count / elapsedSeconds
    )
    println(f"${result.scenario},${result.particles},${result.steps},${result.elapsedMs}%.2f,${result.updatesPerSecond}%.2f,${result.particleUpdatesPerSecond}%.2f")
    require(state.frame > 0L)
    result
  }

  /** Writes dependency-free JSON output suitable for later regression analysis. */
  private def writeJson(results: Vector[Result], path: Path): Unit = {
    Option(path.getParent).foreach(parent => Files.createDirectories(parent))
    val entries = results.map { r =>
      f"""    {"scenario":"${r.scenario}","particles":${r.particles},"steps":${r.steps},"elapsedMs":${r.elapsedMs}%.3f,"updatesPerSecond":${r.updatesPerSecond}%.3f,"particleUpdatesPerSecond":${r.particleUpdatesPerSecond}%.3f}"""
    }.mkString(",\n")
    val json = s"""{
  "version": "0.2.0",
  "results": [
$entries
  ]
}
"""
    Files.write(path, json.getBytes(StandardCharsets.UTF_8))
  }
}
