package io.codeswarm.schauberger.benchmark

import io.codeswarm.schauberger.geometry._
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model._
import io.codeswarm.schauberger.physics.{AxialFlowForce, CompositeFlowForce, FlowForce, SecondaryFlowForce, SwirlForce, WallRepulsionForce}
import io.codeswarm.schauberger.physics.secondary.TwinVortexSecondaryFlow
import io.codeswarm.schauberger.physics.swirl.SwirlProfileFactory
import io.codeswarm.schauberger.simulation.{DefaultGeometryContextCalculator, PipeBoundaryHandler, SecondaryFlowFieldSampler, SemiImplicitEulerIntegrator, SimulationEngine, UniformCrossSectionParticleGenerator}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

/** Milestone 0.6 regression benchmark with profile, geometry and accuracy measurements. */
object SimulationBenchmark {
  private val WarmupIterations = 5
  private val MeasurementIterations = 10

  /** Complete configuration for one simulation benchmark scenario. */
  private final case class Scenario(
                                     name: String,
                                     geometry: PipeGeometry,
                                     profile: VortexProfileParameters,
                                     secondaryEnabled: Boolean
                                   )

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

  /** Accuracy summary for one lookup table size against the exact kernel. */
  private final case class AccuracyResult(
                                           samples: Int,
                                           maxRelativeRadiusError: Double,
                                           meanRelativeRadiusError: Double,
                                           maxNormalAngleErrorDegrees: Double
                                         )

  /** Runs flow, field-sampling, geometry-context and lookup-accuracy benchmark groups. */
  def main(args: Array[String]): Unit = {
    val circular = StraightPipe(1000.0, CircularCrossSection(120.0))
    val ovoidLookup = StraightPipe(1000.0, OvoidCrossSection(220.0, 250.0, 0.14, GeometryEvaluationMode.Lookup, 1024))
    val twistedLookup = TwistedPipe(1000.0, OvoidCrossSection(220.0, 250.0, 0.14, GeometryEvaluationMode.Lookup, 1024), 1.25)

    val scenarios = Vector(
      Scenario("circular-solid-body", circular, SolidBodyParameters, secondaryEnabled = false),
      Scenario("circular-rankine", circular, RankineParameters(0.35), secondaryEnabled = false),
      Scenario("circular-lamb-oseen", circular, LambOseenParameters(1.0, 0.30), secondaryEnabled = false),
      Scenario("ovoid-solid-body", ovoidLookup, SolidBodyParameters, secondaryEnabled = false),
      Scenario("ovoid-rankine", ovoidLookup, RankineParameters(0.35), secondaryEnabled = false),
      Scenario("twisted-ovoid-rankine", twistedLookup, RankineParameters(0.35), secondaryEnabled = false),
      Scenario("twisted-ovoid-rankine-secondary", twistedLookup, RankineParameters(0.35), secondaryEnabled = true)
    )

    val counts = Vector(1000, 10000)
    val results = for {scenario <- scenarios
                       count <- counts} yield runCase(scenario, count)
    val stressResults = Vector(
      runCase(scenarios.head, 50000),
      runCase(scenarios.last, 50000)
    )
    val fieldResults = Vector(15, 30, 60).map(runFieldCase)

    val geometryResults = Vector(
      "circular" -> circular,
      "ovoid-exact" -> StraightPipe(1000.0, OvoidCrossSection(220.0, 250.0, 0.14, GeometryEvaluationMode.Exact, 1024)),
      "ovoid-lookup-1024" -> ovoidLookup,
      "twisted-ovoid-exact" -> TwistedPipe(1000.0, OvoidCrossSection(220.0, 250.0, 0.14, GeometryEvaluationMode.Exact, 1024), 1.25),
      "twisted-ovoid-lookup-1024" -> twistedLookup
    ).map { case (name, geometry) => runGeometryCase(name, geometry) }

    val accuracyResults = Vector(256, 512, 1024, 2048).map(runAccuracyCase)
    writeJson(results ++ stressResults, fieldResults, geometryResults, accuracyResults, Paths.get("benchmark/results/benchmark-0.6.0.json"))
  }

  /** Executes warmup then repeated timed measurements for one scenario. */
  private def runCase(scenario: Scenario, count: Int): Result = {
    val secondaryModel = new TwinVortexSecondaryFlow
    val profileFactory = new SwirlProfileFactory
    val swirlForces: Vector[FlowForce] = Vector(
      new AxialFlowForce,
      new SwirlForce(profileFactory),
      new WallRepulsionForce
    )
    val forces = if (scenario.secondaryEnabled)
      swirlForces.patch(2, Vector(new SecondaryFlowForce(secondaryModel)), 0)
    else swirlForces

    val engine = new SimulationEngine(
      CompositeFlowForce(forces),
      new SemiImplicitEulerIntegrator,
      new UniformCrossSectionParticleGenerator(42L),
      new PipeBoundaryHandler
    )
    val base = SimulationParameters.Default.copy(
      particleCount = count,
      swirl = SimulationParameters.Default.swirl.copy(profile = scenario.profile)
    )
    val parameters = if (scenario.secondaryEnabled) base else base.copy(secondaryFlow = base.secondaryFlow.copy(enabled = false))
    var state = engine.initialState(parameters, scenario.geometry)
    val steps = if (count >= 50000) 100 else 250

    var warmup = 0
    while (warmup < WarmupIterations) {
      var s = 0
      while (s < steps) {
        state = engine.step(state, parameters, scenario.geometry, parameters.fixedTimeStep)
        s += 1
      }
      warmup += 1
    }

    val times = Vector.tabulate(MeasurementIterations) { _ =>
      val started = System.nanoTime()
      var s = 0
      while (s < steps) {
        state = engine.step(state, parameters, scenario.geometry, parameters.fixedTimeStep)
        s += 1
      }
      (System.nanoTime() - started).toDouble / 1_000_000.0
    }.sorted

    val median = percentile(times, 0.50)
    val p95 = percentile(times, 0.95)
    val throughput = steps.toDouble * count / (median / 1000.0)
    val result = Result(scenario.name, count, steps, median, p95, times.head, times.last, throughput)
    println(f"${scenario.name}%-38s $count%7d particles median=$median%.2f ms p95=$p95%.2f ms throughput=$throughput%.2f particle-updates/s")
    result
  }

  /** Measures secondary vector-field generation after warmup. */
  private def runFieldCase(resolution: Int): FieldResult = {
    val model = new TwinVortexSecondaryFlow
    val sampler = new SecondaryFlowFieldSampler(model)
    val geometry = TwistedPipe(1000.0, OvoidCrossSection(220.0, 250.0, 0.14, GeometryEvaluationMode.Lookup, 1024), 1.25)
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

  /** Microbenchmarks the per-particle geometry-context calculation. */
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

  /** Compares lookup radii and normals with the exact ovoid implementation. */
  private def runAccuracyCase(sampleCount: Int): AccuracyResult = {
    val exact = OvoidCrossSection(220.0, 250.0, 0.14, GeometryEvaluationMode.Exact, sampleCount)
    val lookup = OvoidCrossSection(220.0, 250.0, 0.14, GeometryEvaluationMode.Lookup, sampleCount)
    val validationSamples = 10000
    var maxRadiusError = 0.0
    var radiusErrorSum = 0.0
    var maxNormalError = 0.0
    var i = 0
    while (i < validationSamples) {
      val angle = math.Pi * 2.0 * (i + 0.37) / validationSamples.toDouble
      val exactPoint = exact.boundaryPoint(angle)
      val lookupPoint = lookup.boundaryPoint(angle)
      val relativeRadiusError = math.abs(lookupPoint.magnitude - exactPoint.magnitude) / math.max(exactPoint.magnitude, 1e-9)
      maxRadiusError = math.max(maxRadiusError, relativeRadiusError)
      radiusErrorSum += relativeRadiusError
      val exactNormal = exact.inwardNormal(exactPoint)
      val lookupNormal = lookup.inwardNormal(lookupPoint)
      val dot = math.max(-1.0, math.min(1.0, exactNormal.dot(lookupNormal)))
      val angleError = math.toDegrees(math.acos(dot))
      maxNormalError = math.max(maxNormalError, angleError)
      i += 1
    }
    AccuracyResult(sampleCount, maxRadiusError, radiusErrorSum / validationSamples, maxNormalError)
  }

  /** Returns a linearly indexed percentile from sorted values. */
  private def percentile(sorted: Vector[Double], fraction: Double): Double = {
    val index = math.min(sorted.size - 1, math.max(0, math.ceil(sorted.size * fraction).toInt - 1))
    sorted(index)
  }

  /** Writes dependency-free JSON for milestone-to-milestone comparison. */
  private def writeJson(
                         results: Vector[Result],
                         field: Vector[FieldResult],
                         geometry: Vector[GeometryResult],
                         accuracy: Vector[AccuracyResult],
                         path: Path
                       ): Unit = {
    Option(path.getParent).foreach(parent => Files.createDirectories(parent))
    val resultJson = results.map { r =>
      f"""    {"scenario":"${r.scenario}","particles":${r.particles},"stepsPerIteration":${r.stepsPerIteration},"medianMs":${r.medianMs}%.3f,"p95Ms":${r.p95Ms}%.3f,"minMs":${r.minMs}%.3f,"maxMs":${r.maxMs}%.3f,"particleUpdatesPerSecond":${r.particleUpdatesPerSecond}%.3f}"""
    }.mkString(",\n")
    val fieldJson = field.map(r => f"""    {"resolution":${r.resolution},"samples":${r.samples},"medianMs":${r.medianMs}%.3f,"p95Ms":${r.p95Ms}%.3f}""").mkString(",\n")
    val geometryJson = geometry.map(r => f"""    {"geometry":"${r.name}","iterations":${r.iterations},"elapsedMs":${r.elapsedMs}%.3f,"contextsPerSecond":${r.contextsPerSecond}%.3f}""").mkString(",\n")
    val accuracyJson = accuracy.map(r => f"""    {"samples":${r.samples},"maxRelativeRadiusError":${r.maxRelativeRadiusError}%.8f,"meanRelativeRadiusError":${r.meanRelativeRadiusError}%.8f,"maxNormalAngleErrorDegrees":${r.maxNormalAngleErrorDegrees}%.6f}""").mkString(",\n")
    val json =
      s"""{
  "version": "0.6.0",
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
  ],
  "geometryAccuracy": [
$accuracyJson
  ]
}
"""
    Files.write(path, json.getBytes(StandardCharsets.UTF_8))
  }
}
