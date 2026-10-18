package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.geometry.{CircularCrossSection, StraightPipe}
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, RotationDirection, SimulationParameters, VortexRegion}
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RotationSenseClassifierSpec extends AnyFunSuite {
  private val geometry = StraightPipe(100.0, CircularCrossSection(10.0))
  private val classifier = new RotationSenseClassifier
  private val swirl = SimulationParameters.Default.swirl

  // The particle sits on the local y axis, so a velocity along z is tangential and positive z is counter-clockwise.
  private def particle(vz: Double, y: Double = 5.0) = Particle(1L, Vector3D(10.0, y, 0.0), Vector3D(0.0, 0.0, vz))

  test("counter-clockwise motion is forward for the default direction and reverse for clockwise") {
    assert(classifier.classify(particle(2.0), geometry, swirl) == VortexRegion.Forward)
    val clockwise = swirl.copy(rotationDirection = RotationDirection.Clockwise)
    assert(classifier.classify(particle(2.0), geometry, clockwise) == VortexRegion.Reverse)
  }

  test("opposite motion is reverse") {
    assert(classifier.classify(particle(-2.0), geometry, swirl) == VortexRegion.Reverse)
  }

  test("slow particles and particles on the axis are quiescent") {
    assert(classifier.classify(particle(0.0), geometry, swirl) == VortexRegion.Quiescent)
    assert(classifier.classify(particle(3.0, y = 0.0), geometry, swirl) == VortexRegion.Quiescent)
  }

  test("agrees with the aggregate region metrics") {
    val metrics = new io.codeswarm.schauberger.simulation.FlowMetricsCalculator(new io.codeswarm.schauberger.physics.secondary.TwinVortexSecondaryFlow)
    val velocities = Seq(2.0, -2.0, 0.0, 1.5, -0.5)
    val particles = velocities.zipWithIndex.map { case (v, i) => particle(v).copy(id = i.toLong) }.toVector
    val counts = particles.map(classifier.classify(_, geometry, swirl)).groupBy(identity).view.mapValues(_.size).toMap
    val regions = metrics.calculate(io.codeswarm.schauberger.model.SimulationState(particles, 0.0, 0L), geometry, SimulationParameters.Default).regions
    assert(math.abs(regions.forwardShare - counts.getOrElse(VortexRegion.Forward, 0).toDouble / particles.size) < 1e-12)
    assert(math.abs(regions.reverseShare - counts.getOrElse(VortexRegion.Reverse, 0).toDouble / particles.size) < 1e-12)
  }

  test("each region has its own colour") {
    val colors = Seq(VortexRegion.Forward, VortexRegion.Reverse, VortexRegion.Quiescent).map(RotationSensePalette.hex)
    assert(colors.distinct.size == 3)
    assert(!colors.contains(RotationSensePalette.Plain))
  }
}
