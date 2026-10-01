package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.{OvoidCrossSection,StraightPipe}
import io.codeswarm.schauberger.model.{Particle,SimulationParameters}
import io.codeswarm.schauberger.math.Vector3D
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class WallRepulsionForceSpec extends AnyFunSuite {
  test("wall force uses geometry normal rather than circular radius") {
    val pipe=StraightPipe(100,OvoidCrossSection(20,30,0.2)); val p=Particle(1,Vector3D(20,9.8,0),Vector3D.Zero)
    val a=new WallRepulsionForce.acceleration(p,FlowContext(pipe,SimulationParameters.Default.copy(wallThreshold=3)))
    assert(a.dot(pipe.inwardNormal(p.position))>=0)
  }
}
