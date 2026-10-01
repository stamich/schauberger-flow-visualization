package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.{OvoidCrossSection, TwistedPipe}
import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
import io.codeswarm.schauberger.model.Particle
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GeometryContextCalculatorSpec extends AnyFunSuite {
  test("context contains local position, finite normal and twist metadata") {
    val geometry = TwistedPipe(100.0, OvoidCrossSection(20.0, 30.0, 0.1), 1.0)
    val local = Vector2D(3.0, -2.0)
    val particle = Particle(1L, geometry.fromLocalCrossSection(25.0, local), Vector3D.Zero)
    val context = new DefaultGeometryContextCalculator().calculate(particle, geometry)
    assert((context.localPosition - local).magnitude < 1e-8)
    assert(context.inwardNormal.x.isFinite && context.inwardNormal.y.isFinite && context.inwardNormal.z.isFinite)
    assert(math.abs(context.twistAngle - math.Pi / 2.0) < 1e-8)
    assert(math.abs(context.twistRate - 2.0 * math.Pi / 100.0) < 1e-8)
  }
}
