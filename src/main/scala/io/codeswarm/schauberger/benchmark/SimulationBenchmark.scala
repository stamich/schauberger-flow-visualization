package io.codeswarm.schauberger.benchmark

import io.codeswarm.schauberger.geometry.{CircularCrossSection, OvoidCrossSection, PipeGeometry, StraightPipe, TwistedPipe}
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}
import io.codeswarm.schauberger.physics.{AxialFlowForce, CompositeFlowForce, FlowForce, SecondaryFlowForce, SolidBodySwirlProfile, SwirlForce, WallRepulsionForce}
import io.codeswarm.schauberger.physics.secondary.TwinVortexSecondaryFlow
import io.codeswarm.schauberger.simulation.{DefaultGeometryContextCalculator, PipeBoundaryHandler, SecondaryFlowFieldSampler, SemiImplicitEulerIntegrator, SimulationEngine, UniformCrossSectionParticleGenerator}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

/** Milestone 0.5 regression benchmark with JVM warmup and repeated measurements. */
object SimulationBenchmark {
  private val WarmupIterations = 5
  private val MeasurementIterations = 10

  /** Statistical summary for one scenario/population pair. */
  private final case class Result(
                                   scenario: String,
                                   particles: Int,
                                   stepsPerIteration: Int,
                                   medianMs: Double,
                                   p95Ms: Double,
                                   minMs: Double,
                                   maxMs: Double,
                                   particleUpdatesPerSecond: Double
                                 )

  /** Summary for one secondary-field sampling resolution. */
  private final case class FieldResult(resolution: Int, samples: Int, medianMs: Double, p95Ms: Double)

  /** Summary for one geometry-context microbenchmark. */
  private final case class GeometryResult(name: String, iterations: Int, elapsedMs: Double, contextsPerSecond: Double)

  /** Runs flow, field-sampling and geometry-context benchmark groups. */
  def main(args: Array[String]): Unit = {
    val scenarios = Vector(
      "circular-axial", "circular-swirl", "ovoid-swirl",
      "twisted-ovoid-swirl", "twisted-ovoid-swirl-secondary"
    )
    val counts = Vector(1000, 10000, 50000)
    val results = for {scenario <- scenarios
                       count <- counts} yield runCase(scenario, count)
    val fieldResults = Vector(15, 30, 60).map(runFieldCase)
    val geometryResults = Vector(
      "circular" -> StraightPipe(1000.0, CircularCrossSection(120.0)),
      "ovoid" -> StraightPipe(1000.0, OvoidCrossSection(220.0, 250.0, 0.14)),
      "twisted-ovoid" -> TwistedPipe(1000.0, OvoidCrossSection(220.0, 250.0, 0.14), 1.25)
    ).map { case (name, geometry) => runGeometryCase(name, geometry) }
    writeJson(results, fieldResults, geometryResults, Paths.get("benchmark/results/benchmark-0.5.0.json"))
  }

  /** Creates benchmark geometry for a scenario. */
  private def geometryFor(scenario: String): PipeGeometry = scenario match {
    case "circular-axial" | "circular-swirl" => StraightPipe(1000.0, CircularCrossSection(120.0))
    case "ovoid-swirl" => StraightPipe(1000.0, OvoidCrossSection(220.0, 250.0, 0.14))
    case "twisted-ovoid-swirl" | "twisted-ovoid-swirl-secondary" =>
      TwistedPipe(1000.0, OvoidCrossSection(220.0, 250.0, 0.14), 1.25)
    case other => throw new IllegalArgumentException(s"Unknown benchmark scenario: $other")
  }

  /** Executes warmup then repeated timed measurements. */
  private def runCase(scenario: String, count: Int): Result = {
    val secondaryModel = new TwinVortexSecondaryFlow
    val swirlForces: Vector[FlowForce] = Vector(new AxialFlowForce, new SwirlForce(new SolidBodySwirlProfile), new WallRepulsionForce)
    val forces: Vector[FlowForce] = scenario match {
      case "circular-axial" => Vector(new AxialFlowForce, new WallRepulsionForce)
      case "twisted-ovoid-swirl-secondary" => swirlForces.patch(2, Vector(new SecondaryFlowForce(secondaryModel)), 0)
      case _ => swirlForces
    }
    val engine = new SimulationEngine(CompositeFlowForce(forces), new SemiImplicitEulerIntegrator, new UniformCrossSectionParticleGenerator(42L), new PipeBoundaryHandler)
    val geometry = geometryFor(scenario)
    val base = SimulationParameters.Default.copy(particleCount = count)
    val parameters = if (scenario == "twisted-ovoid-swirl-secondary") base else base.copy(secondaryFlow = base.secondaryFlow.copy(enabled = false))
    var state = engine.initialState(parameters, geometry)
    val steps = if (count >= 50000) 100 else 250

    var warmup = 0
    while (warmup < WarmupIterations) {
      var s = 0
      while (s < steps) {
        state = engine.step(state, parameters, geometry, parameters.fixedTimeStep)
        s += 1
      }
      warmup += 1
    }

    val times = Vector.tabulate(MeasurementIterations) { _ =>
      val started = System.nanoTime()
      var s = 0
      while (s < steps) {
        state = engine.step(state, parameters, geometry, parameters.fixedTimeStep)
        s += 1
      }
      (System.nanoTime() - started).toDouble / 1_000_000.0
    }.sorted

    val median = percentile(times, 0.50)
    val p95 = percentile(times, 0.95)
    val seconds = median / 1000.0
    val throughput = steps.toDouble * count / seconds
    val result = Result(scenario, count, steps, median, p95, times.head, times.last, throughput)
    println(f"$scenario%-34s $count%7d particles median=${median}%.2f ms p95=${p95}%.2f ms throughput=${throughput}%.2f particle-updates/s")
    result
  }

  /** Measures secondary vector-field generation after warmup. */
  private def runFieldCase(resolution: Int): FieldResult = {
    val model = new TwinVortexSecondaryFlow
    val sampler = new SecondaryFlowFieldSampler(model)
    val geometry = TwistedPipe(1000.0, OvoidCrossSection(220.0, 250.0, 0.14), 1.25)
    (0 until 5).foreach(_ => sampler.sample(geometry, 500.0, SimulationParameters.Default.secondaryFlow, resolution))
    var lastSamples = 0
    val times = Vector.tabulate(10) { _ =>
      val started = System.nanoTime()
      val samples = sampler.sample(geometry, 500.0, SimulationParameters.Default.secondaryFlow, resolution)
      lastSamples = samples.size
      (System.nanoTime() - started).toDouble / 1_000_000.0
    }.sorted
    FieldResult(resolution, lastSamples, percentile(times, 0.50), percentile(times, 0.95))
  }

  /** Microbenchmarks the milestone 0.5 per-particle geometry context calculation. */
  private def runGeometryCase(name: String, geometry: PipeGeometry): GeometryResult = {
    val calculator = new DefaultGeometryContextCalculator
    val count = 500000
    val particle = Particle(1L, geometry.fromLocalCrossSection(geometry.length * 0.5, geometry.crossSection.boundaryPoint(0.7) * 0.6), Vector3D.Zero)
    var warmup = 0
    while (warmup < 50000) {
      calculator.calculate(particle, geometry)
      warmup += 1
    }
    val started = System.nanoTime()
    var i = 0
    while (i < count) {
      calculator.calculate(particle, geometry)
      i += 1
    }
    val elapsedSeconds = (System.nanoTime() - started).toDouble / 1_000_000_000.0
    GeometryResult(name, count, elapsedSeconds * 1000.0, count / elapsedSeconds)
  }

  /** Returns a linearly indexed percentile from sorted values. */
  private def percentile(sorted: Vector[Double], fraction: Double): Double = {
    val index = math.min(sorted.size - 1, math.max(0, math.ceil(sorted.size * fraction).toInt - 1))
    sorted(index)
  }

  /** Writes dependency-free JSON for milestone-to-milestone comparison. */
  private def writeJson(results: Vector[Result], field: Vector[FieldResult], geometry: Vector[GeometryResult], path: Path): Unit = {
    Option(path.getParent).foreach(parent => Files.createDirectories(parent))
    val resultJson = results.map { r =>
      f"""    {"scenario":"${r.scenario}","particles":${r.particles},"stepsPerIteration":${r.stepsPerIteration},"medianMs":${r.medianMs}%.3f,"p95Ms":${r.p95Ms}%.3f,"minMs":${r.minMs}%.3f,"maxMs":${r.maxMs}%.3f,"particleUpdatesPerSecond":${r.particleUpdatesPerSecond}%.3f}"""
    }.mkString(",")
    val fieldJson = field.map(r => f"""    {"resolution":${r.resolution},"samples":${r.samples},"medianMs":${r.medianMs}%.3f,"p95Ms":${r.p95Ms}%.3f}""").mkString(",")
    val geometryJson = geometry.map(r => f"""    {"geometry":"${r.name}","iterations":${r.iterations},"elapsedMs":${r.elapsedMs}%.3f,"contextsPerSecond":${r.contextsPerSecond}%.3f}""").mkString(",+")
    val json =
      s"""{
  "version": "0.5.0",
  "warmupIterations": $WarmupIterations,
  "measurementIterations": $MeasurementIterations,
  "results": [
$resultJson
  ],
  "fieldSampling": [
$fieldJson
  ],
  "geometryContext": [
$geometryJson
  ]
}
"""
    Files.write(path, json.getBytes(StandardCharsets.UTF_8))
  }
}
