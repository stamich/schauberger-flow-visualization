package io.codeswarm.schauberger.benchmark

import io.codeswarm.schauberger.geometry.{CircularCrossSection, OvoidCrossSection, PipeGeometry, StraightPipe, TwistedPipe}
import io.codeswarm.schauberger.model.SimulationParameters
import io.codeswarm.schauberger.physics.{AxialFlowForce, CompositeFlowForce, FlowForce, SecondaryFlowForce, SolidBodySwirlProfile, SwirlForce, WallRepulsionForce}
import io.codeswarm.schauberger.physics.secondary.TwinVortexSecondaryFlow
import io.codeswarm.schauberger.simulation.{PipeBoundaryHandler, SecondaryFlowFieldSampler, SemiImplicitEulerIntegrator, SimulationEngine, UniformCrossSectionParticleGenerator}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

/** Lightweight regression benchmark comparing milestone 0.4 flow-model costs.
  *
  * It is intended for milestone-to-milestone regression tracking rather than
  * rigorous JVM microbenchmarking; use JMH for publication-grade measurements.
  */
object SimulationBenchmark {

  /** One measured particle-engine benchmark row. */
  private final case class Result(
      scenario: String,
      particles: Int,
      steps: Int,
      elapsedMs: Double,
      updatesPerSecond: Double,
      particleUpdatesPerSecond: Double
  )

  /** One measured field-sampling benchmark row. */
  private final case class FieldResult(resolution: Int, samples: Int, elapsedMs: Double)

  /** Runs five particle scenarios and three secondary-field sampling scenarios. */
  def main(args: Array[String]): Unit = {
    val scenarios = Vector(
      "circular-axial",
      "circular-swirl",
      "ovoid-swirl",
      "twisted-ovoid-swirl",
      "twisted-ovoid-swirl-secondary"
    )
    val counts = Vector(1000, 10000, 50000)
    println("scenario,particles,steps,elapsed_ms,updates_per_second,particle_updates_per_second")
    val results = for {
      scenario <- scenarios
      count <- counts
    } yield runCase(scenario, count)

    val fieldResults = Vector(15, 30, 60).map(runFieldCase)
    writeJson(results, fieldResults, Paths.get("benchmark/results/benchmark-0.4.0.json"))
  }

  /** Creates the geometry associated with a benchmark scenario. */
  private def geometryFor(scenario: String): PipeGeometry = scenario match {
    case "circular-axial" | "circular-swirl" => StraightPipe(1000.0, CircularCrossSection(120.0))
    case "ovoid-swirl" => StraightPipe(1000.0, OvoidCrossSection(220.0, 250.0, 0.14))
    case "twisted-ovoid-swirl" | "twisted-ovoid-swirl-secondary" =>
      TwistedPipe(1000.0, OvoidCrossSection(220.0, 250.0, 0.14), 1.25)
    case other => throw new IllegalArgumentException(s"Unknown benchmark scenario: $other")
  }

  /** Executes deterministic warmup and measurement for one scenario/population pair. */
  private def runCase(scenario: String, count: Int): Result = {
    val secondaryModel = new TwinVortexSecondaryFlow
    val swirlForces: Vector[FlowForce] = Vector(
      new AxialFlowForce,
      new SwirlForce(new SolidBodySwirlProfile),
      new WallRepulsionForce
    )
    val forces: Vector[FlowForce] = scenario match {
      case "circular-axial" => Vector(new AxialFlowForce, new WallRepulsionForce)
      case "twisted-ovoid-swirl-secondary" => swirlForces.patch(2, Vector(new SecondaryFlowForce(secondaryModel)), 0)
      case _ => swirlForces
    }

    val engine = new SimulationEngine(
      CompositeFlowForce(forces),
      new SemiImplicitEulerIntegrator,
      new UniformCrossSectionParticleGenerator(42L),
      new PipeBoundaryHandler
    )
    val geometry = geometryFor(scenario)
    val base = SimulationParameters.Default.copy(particleCount = count)
    val parameters = if (scenario == "twisted-ovoid-swirl-secondary") base else base.copy(secondaryFlow = base.secondaryFlow.copy(enabled = false))
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

  /** Measures generation of a secondary vector field at one grid resolution. */
  private def runFieldCase(resolution: Int): FieldResult = {
    val model = new TwinVortexSecondaryFlow
    val sampler = new SecondaryFlowFieldSampler(model)
    val geometry = TwistedPipe(1000.0, OvoidCrossSection(220.0, 250.0, 0.14), 1.25)
    val started = System.nanoTime()
    var samples = Vector.empty[io.codeswarm.schauberger.simulation.FieldSample]
    var i = 0
    while (i < 100) {
      samples = sampler.sample(geometry, 500.0, SimulationParameters.Default.secondaryFlow, resolution)
      i += 1
    }
    val elapsedMs = (System.nanoTime() - started).toDouble / 1_000_000.0
    FieldResult(resolution, samples.size, elapsedMs)
  }

  /** Writes dependency-free JSON output for later milestone comparisons. */
  private def writeJson(results: Vector[Result], fieldResults: Vector[FieldResult], path: Path): Unit = {
    Option(path.getParent).foreach(parent => Files.createDirectories(parent))
    val entries = results.map { result =>
      f"""    {"scenario":"${result.scenario}","particles":${result.particles},"steps":${result.steps},"elapsedMs":${result.elapsedMs}%.3f,"updatesPerSecond":${result.updatesPerSecond}%.3f,"particleUpdatesPerSecond":${result.particleUpdatesPerSecond}%.3f}"""
    }.mkString(",\n")
    val fieldEntries = fieldResults.map { result =>
      f"""    {"resolution":${result.resolution},"samples":${result.samples},"elapsedMs":${result.elapsedMs}%.3f}"""
    }.mkString(",\n")
    val json = s"""{
  "version": "0.4.0",
  "results": [
$entries
  ],
  "fieldSampling": [
$fieldEntries
  ]
}
"""
    Files.write(path, json.getBytes(StandardCharsets.UTF_8))
  }
}
