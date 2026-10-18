package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.{GeometryFactory, OvoidCrossSection, PipeGeometry, TwistedPipe}
import io.codeswarm.schauberger.model.CounterRotatingParameters
import io.codeswarm.schauberger.model.{FlowMetrics, GeometryParameters, SimulationParameters}
import io.codeswarm.schauberger.physics.DefaultFlowForces
import io.codeswarm.schauberger.physics.secondary.TwinVortexSecondaryFlow
import io.codeswarm.schauberger.physics.swirl.SwirlProfileFactory
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SimulationEngineSpec extends AnyFunSuite {
  private val secondaryModel = new TwinVortexSecondaryFlow
  private val engine = new SimulationEngine(
    DefaultFlowForces.create(new SwirlProfileFactory, secondaryModel),
    new SemiImplicitEulerIntegrator,
    new UniformCrossSectionParticleGenerator(42L),
    new PipeBoundaryHandler
  )

  test("long run preserves population, finite vectors and geometry invariant") {
    val geometry = TwistedPipe(300.0, OvoidCrossSection(80.0, 100.0, 0.14), 1.25)
    val parameters = SimulationParameters.Default.copy(particleCount = 120)
    var state = engine.initialState(parameters, geometry)
    var index = 0
    while (index < 2000) {
      state = engine.step(state, parameters, geometry, parameters.fixedTimeStep)
      index += 1
    }
    assert(state.particles.size == 120)
    assert(state.particles.forall(particle => geometry.contains(particle.position)))
    assert(state.particles.forall(particle => Seq(
      particle.position.x, particle.position.y, particle.position.z,
      particle.velocity.x, particle.velocity.y, particle.velocity.z
    ).forall(value => !value.isNaN && !value.isInfinity)))
  }

  test("secondary flow changes trajectory while preserving containment") {
    val geometry = TwistedPipe(300.0, OvoidCrossSection(80.0, 100.0, 0.10), 1.0)
    val enabled = SimulationParameters.Default.copy(particleCount = 1)
    val disabled = enabled.copy(secondaryFlow = enabled.secondaryFlow.copy(enabled = false))
    var enabledState = engine.initialState(enabled, geometry)
    var disabledState = engine.initialState(disabled, geometry)
    var index = 0
    while (index < 500) {
      enabledState = engine.step(enabledState, enabled, geometry, enabled.fixedTimeStep)
      disabledState = engine.step(disabledState, disabled, geometry, disabled.fixedTimeStep)
      index += 1
    }
    val enabledPosition = enabledState.particles.head.position
    val disabledPosition = disabledState.particles.head.position
    assert((enabledPosition - disabledPosition).magnitude > 1e-4)
    assert(geometry.contains(enabledPosition))
    assert(geometry.contains(disabledPosition))
  }

  /** Runs a deterministic population and reports its aggregate metrics, including radial occupancy. */
  private def metricsAfter(geometry: PipeGeometry, parameters: SimulationParameters, steps: Int): FlowMetrics = {
    var state = engine.initialState(parameters, geometry)
    var index = 0
    while (index < steps) {
      state = engine.step(state, parameters, geometry, parameters.fixedTimeStep)
      index += 1
    }
    new FlowMetricsCalculator(secondaryModel).calculate(state, geometry, parameters)
  }

  private val defaultGeometry = new GeometryFactory().create(GeometryParameters.Default)
  private val swirlOnly = {
    val base = SimulationParameters.Default.copy(particleCount = 600)
    base.copy(secondaryFlow = base.secondaryFlow.copy(enabled = false))
  }
  private val noSwirlReference = swirlOnly.copy(swirl = swirlOnly.swirl.copy(angularVelocity = 0.0))
  private val longRun = 2400

  test("swirl with centripetal balance leaves the radial distribution at its no-swirl level") {
    val reference = metricsAfter(defaultGeometry, noSwirlReference, longRun)
    val swirling = metricsAfter(defaultGeometry, swirlOnly, longRun)
    assert(swirling.meanTangentialVelocity > 10.0, "the population must actually be swirling")
    assert(math.abs(swirling.occupancy.outer - reference.occupancy.outer) < 0.10)
    assert(math.abs(swirling.meanNormalizedRadialPosition - reference.meanNormalizedRadialPosition) < 0.08)
  }

  test("without the balance the same swirl throws the population into the outer third") {
    val reference = metricsAfter(defaultGeometry, noSwirlReference, longRun)
    val unbalanced = swirlOnly.copy(radialBalance = swirlOnly.radialBalance.copy(enabled = false))
    val swirling = metricsAfter(defaultGeometry, unbalanced, longRun)
    assert(swirling.occupancy.outer > reference.occupancy.outer + 0.30)
    assert(swirling.meanNormalizedRadialPosition > reference.meanNormalizedRadialPosition + 0.20)
  }

  test("secondary flow no longer halves the swirl") {
    val withSecondary = swirlOnly.copy(secondaryFlow = SimulationParameters.Default.secondaryFlow)
    val alone = metricsAfter(defaultGeometry, swirlOnly, longRun)
    val combined = metricsAfter(defaultGeometry, withSecondary, longRun)
    // Two independent relaxations used to leave about 53% of the swirl; one target keeps all of it.
    assert(combined.meanAngularVelocity > 0.85 * alone.meanAngularVelocity)
    assert(combined.meanAngularVelocity < 1.25 * alone.meanAngularVelocity)
  }

  private val counterRotating = swirlOnly.copy(swirl = swirlOnly.swirl.copy(profile = CounterRotatingParameters()))

  test("counter-rotating swirl produces particles turning both ways") {
    val metrics = metricsAfter(defaultGeometry, counterRotating, longRun)
    assert(metrics.regions.forwardShare > 0.25)
    assert(metrics.regions.reverseShare > 0.25)
    assert(metrics.regions.isCounterRotating())
  }

  test("counter-rotating swirl does not cancel to zero in the absolute measure") {
    val metrics = metricsAfter(defaultGeometry, counterRotating, longRun)
    assert(metrics.regions.meanAbsoluteAngular > 0.30)
  }

  test("counter-rotating swirl with balance leaves the radial distribution near its no-swirl level") {
    val reference = metricsAfter(defaultGeometry, noSwirlReference, longRun)
    val metrics = metricsAfter(defaultGeometry, counterRotating, longRun)
    assert(math.abs(metrics.occupancy.outer - reference.occupancy.outer) < 0.12)
    assert(math.abs(metrics.meanNormalizedRadialPosition - reference.meanNormalizedRadialPosition) < 0.10)
  }

  test("counter-rotating run stays finite with every particle inside the pipe") {
    var state = engine.initialState(counterRotating, defaultGeometry)
    (0 until 1200).foreach(_ => state = engine.step(state, counterRotating, defaultGeometry, counterRotating.fixedTimeStep))
    assert(state.particles.size == counterRotating.particleCount)
    assert(state.particles.forall(p => Seq(p.position.x, p.position.y, p.position.z, p.velocity.x, p.velocity.y, p.velocity.z)
      .forall(value => !value.isNaN && !value.isInfinity)))
    assert(state.particles.forall(p => defaultGeometry.contains(p.position)))
  }
}
