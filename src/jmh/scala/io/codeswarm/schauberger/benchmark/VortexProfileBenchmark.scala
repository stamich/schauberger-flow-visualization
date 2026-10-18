package io.codeswarm.schauberger.benchmark

import io.codeswarm.schauberger.physics.swirl.{CounterRotatingVortexProfile, LambOseenVortexProfile, RankineVortexProfile, SolidBodySwirlProfile, TanhTransition}
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

/** JMH microbenchmark for the single vortex profiles and the counter-rotating blend. */
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 8)
@Fork(1)
@State(Scope.Benchmark)
class VortexProfileBenchmark {
  private val rankine = RankineVortexProfile(0.35)
  private val lambOseen = LambOseenVortexProfile(1.0, 0.30)
  private val counterRotating = CounterRotatingVortexProfile(RankineVortexProfile(0.4), SolidBodySwirlProfile, 1.0, 0.6, TanhTransition(0.55, 0.08))
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

  /** Benchmarks the counter-rotating blend, which evaluates two layers and a tanh. */
  @Benchmark
  def counterRotatingProfile(blackhole: Blackhole): Unit =
    blackhole.consume(counterRotating.tangentialVelocity(radius, 100.0, 0.55))
}
