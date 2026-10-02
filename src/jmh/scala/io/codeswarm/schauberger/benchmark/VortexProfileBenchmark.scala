package io.codeswarm.schauberger.benchmark

import io.codeswarm.schauberger.physics.swirl.{LambOseenVortexProfile, RankineVortexProfile, SolidBodySwirlProfile}
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

/** JMH microbenchmark for the three milestone 0.6 vortex profiles. */
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 8)
@Fork(1)
@State(Scope.Benchmark)
class VortexProfileBenchmark {
  private val rankine = RankineVortexProfile(0.35)
  private val lambOseen = LambOseenVortexProfile(1.0, 0.30)
  private var radius = 42.0

  /** Benchmarks solid-body target velocity. */
  @Benchmark
  def solidBody(blackhole: Blackhole): Unit =
    blackhole.consume(SolidBodySwirlProfile.tangentialVelocity(radius, 100.0, 0.55))

  /** Benchmarks Rankine target velocity. */
  @Benchmark
  def rankineProfile(blackhole: Blackhole): Unit =
    blackhole.consume(rankine.tangentialVelocity(radius, 100.0, 0.55))

  /** Benchmarks Lamb-Oseen target velocity. */
  @Benchmark
  def lambOseenProfile(blackhole: Blackhole): Unit =
    blackhole.consume(lambOseen.tangentialVelocity(radius, 100.0, 0.55))
}
