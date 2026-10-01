package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.{CircularCrossSection,StraightPipe}
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle,SimulationState}
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class FlowMetricsCalculatorSpec extends AnyFunSuite {
  test("normalized radial metric is zero on centerline") { val g=StraightPipe(100,CircularCrossSection(10));val m=new FlowMetricsCalculator.calculate(SimulationState(Vector(Particle(1,Vector3D(10,0,0),Vector3D(5,0,0))),0,0),g);assert(m.meanNormalizedRadialPosition==0.0);assert(m.meanAxialVelocity==5.0) }
}
