package io.codeswarm.schauberger.benchmark

import io.codeswarm.schauberger.geometry.{CircularCrossSection, OvoidCrossSection, PipeGeometry, StraightPipe, TwistedPipe}
import io.codeswarm.schauberger.model.SimulationParameters
import io.codeswarm.schauberger.physics.{AxialFlowForce, CompositeFlowForce, FlowForce, SolidBodySwirlProfile, SwirlForce, WallRepulsionForce}
import io.codeswarm.schauberger.simulation.{PipeBoundaryHandler, SemiImplicitEulerIntegrator, SimulationEngine, UniformCrossSectionParticleGenerator}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

/** Lightweight regression benchmark comparing milestone 0.3 geometry costs.
  *
  * It is intended to detect milestone-to-milestone regressions. Use JMH for rigorous
  * JVM microbenchmarking.
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

  /** Runs four geometry/flow scenarios for three particle populations. */
  def main(args: Array[String]): Unit = {
    val scenarios = Vector("circular-axial", "circular-swirl", "ovoid-swirl", "twisted-ovoid-swirl")
    val counts = Vector(1000, 10000, 50000)
    println("scenario,particles,steps,elapsed_ms,updates_per_second,particle_updates_per_second")
    val results = for {
      scenario <- scenarios
      count <- counts
    } yield runCase(scenario, count)
    writeJson(results, Paths.get("benchmark/results/benchmark-0.3.0.json"))
  }

  /** Creates the geometry associated with a benchmark scenario. */
  private def geometryFor(scenario: String): PipeGeometry = scenario match {
    case "circular-axial" | "circular-swirl" => StraightPipe(1000.0, CircularCrossSection(120.0))
    case "ovoid-swirl" => StraightPipe(1000.0, OvoidCrossSection(220.0, 250.0, 0.14))
    case "twisted-ovoid-swirl" => TwistedPipe(1000.0, OvoidCrossSection(220.0, 250.0, 0.14), 1.25)
    case other => throw new IllegalArgumentException(s"Unknown benchmark scenario: $other")
  }

  /** Executes deterministic warmup and measurement for one scenario/population pair. */
  private def runCase(scenario: String, count: Int): Result = {
    val forces: Vector[FlowForce] =
      if (scenario == "circular-axial") Vector(new AxialFlowForce, new WallRepulsionForce)
      else Vector(new AxialFlowForce, new SwirlForce(new SolidBodySwirlProfile), new WallRepulsionForce)

    val engine = new SimulationEngine(
      CompositeFlowForce(forces),
      new SemiImplicitEulerIntegrator,
      new UniformCrossSectionParticleGenerator(42L),
      new PipeBoundaryHandler
    )
    val geometry = geometryFor(scenario)
    val parameters = SimulationParameters.Default.copy(particleCount = count)
    var state = engine.initialState(parameters, geometry)

    var warmup = 0
    while (warmup < 120) {
      state = engine.step(state, parameters, geometry, parameters.fixedTimeStep)
      warmup += 1
    }

    val measuredSteps = if (count >= 50000) 180 else 500
    val started = System.nanoTime()
    var step = 0
    while (step < measuredSteps) {
      state = engine.step(state, parameters, geometry, parameters.fixedTimeStep)
      step += 1
    }

    val elapsedSeconds = (System.nanoTime() - started).toDouble / 1_000_000_000.0
    val result = Result(
      scenario = scenario,
      particles = count,
      steps = measuredSteps,
      elapsedMs = elapsedSeconds * 1000.0,
      updatesPerSecond = measuredSteps / elapsedSeconds,
      particleUpdatesPerSecond = measuredSteps.toDouble * count / elapsedSeconds
    )
    println(f"${result.scenario},${result.particles},${result.steps},${result.elapsedMs}%.2f,${result.updatesPerSecond}%.2f,${result.particleUpdatesPerSecond}%.2f")
    require(state.frame > 0L)
    result
  }

  /** Writes dependency-free JSON output for later milestone comparisons. */
  private def writeJson(results: Vector[Result], path: Path): Unit = {
    Option(path.getParent).foreach(parent => Files.createDirectories(parent))
    val entries = results.map { result =>
      f"""    {"scenario":"${result.scenario}","particles":${result.particles},"steps":${result.steps},"elapsedMs":${result.elapsedMs}%.3f,"updatesPerSecond":${result.updatesPerSecond}%.3f,"particleUpdatesPerSecond":${result.particleUpdatesPerSecond}%.3f}"""
    }.mkString(",\n")
    val json = s"""{
  "version": "0.3.0",
  "results": [
$entries
  ]
}
"""
    Files.write(path, json.getBytes(StandardCharsets.UTF_8))
  }
}
