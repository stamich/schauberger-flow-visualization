package io.codeswarm.schauberger.benchmark

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

  /** Prepares immutable kernels once per benchmark state. */
  @Setup
  def setup(): Unit = {
    exact = new ExactOvoidGeometryKernel(110.0, 125.0, 0.14)
    lookup = new LookupOvoidGeometryKernel(OvoidBoundaryLookup.build(110.0, 125.0, 0.14, 1024))
  }

  /** Benchmarks exact analytic radius/normal evaluation. */
  @Benchmark
  def exactKernel(blackhole: Blackhole): Unit = {
    angle += 1e-6
    blackhole.consume(exact.radiusAt(angle))
    blackhole.consume(exact.normalAt(angle))
  }

  /** Benchmarks lookup-interpolated radius/normal evaluation. */
  @Benchmark
  def lookupKernel(blackhole: Blackhole): Unit = {
    angle += 1e-6
    blackhole.consume(lookup.radiusAt(angle))
    blackhole.consume(lookup.normalAt(angle))
  }
}
