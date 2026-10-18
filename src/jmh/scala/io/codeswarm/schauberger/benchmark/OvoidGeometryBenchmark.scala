package io.codeswarm.schauberger.benchmark

import io.codeswarm.schauberger.geometry.OvoidCrossSection
import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.geometry.ovoid.{ExactOvoidGeometryKernel, LookupOvoidGeometryKernel, OvoidBoundaryLookup, OvoidGeometryKernel}
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

/** JMH microbenchmark comparing exact and lookup ovoid boundary kernels. */
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 8)
@Fork(1)
@State(Scope.Benchmark)
class OvoidGeometryBenchmark {
  private var exact: OvoidGeometryKernel = _
  private var lookup: OvoidGeometryKernel = _
  private var angle: Double = 0.731
  private val section = OvoidCrossSection(220.0, 250.0, 0.14)

  /** Prepares immutable kernels once per benchmark state. */
  @Setup
  def setup(): Unit = {
    exact = new ExactOvoidGeometryKernel(110.0, 125.0, 0.14)
    lookup = new LookupOvoidGeometryKernel(OvoidBoundaryLookup.build(110.0, 125.0, 0.14, 512))
  }

  /** Benchmarks exact analytic radius/normal evaluation. */
  @Benchmark
  def exactKernel(blackhole: Blackhole): Unit = {
    angle += 1e-6
    blackhole.consume(exact.radiusAt(angle))
    blackhole.consume(exact.normalAt(angle))
  }

  /** Benchmarks lookup-interpolated radius/normal evaluation with two separate lookups, as in 0.6. */
  @Benchmark
  def lookupKernel(blackhole: Blackhole): Unit = {
    angle += 1e-6
    blackhole.consume(lookup.radiusAt(angle))
    blackhole.consume(lookup.normalAt(angle))
  }

  /** Benchmarks lookup radius and normal obtained from a single ray evaluation. */
  @Benchmark
  def lookupRay(blackhole: Blackhole): Unit = {
    angle += 1e-6
    blackhole.consume(lookup.rayAt(angle))
  }

  /** Benchmarks the cross-section distance and normal as two calls. */
  @Benchmark
  def crossSectionSeparate(blackhole: Blackhole): Unit = {
    angle += 1e-6
    val point = Vector2D(60.0 * math.cos(angle), 60.0 * math.sin(angle))
    blackhole.consume(section.signedDistance(point))
    blackhole.consume(section.inwardNormal(point))
  }

  /** Benchmarks the cross-section distance and normal as one query. */
  @Benchmark
  def crossSectionQuery(blackhole: Blackhole): Unit = {
    angle += 1e-6
    val point = Vector2D(60.0 * math.cos(angle), 60.0 * math.sin(angle))
    blackhole.consume(section.boundaryQuery(point))
  }
}
