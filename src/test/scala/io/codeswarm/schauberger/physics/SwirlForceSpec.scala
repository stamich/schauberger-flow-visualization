package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.{OvoidCrossSection,TwistedPipe}
import io.codeswarm.schauberger.math.{Vector2D,Vector3D}
import io.codeswarm.schauberger.model.{Particle,RotationDirection,SimulationParameters}
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class SwirlForceSpec extends AnyFunSuite {
  private val pipe=TwistedPipe(100,OvoidCrossSection(20,30,0.1),1)
  test("swirl is tangential and reverses with rotation direction") {
    val pos=pipe.fromLocalCrossSection(25,Vector2D(5,0)); val particle=Particle(1,pos,Vector3D.Zero); val force=new SwirlForce(new SolidBodySwirlProfile)
    val ccw=force.acceleration(particle,FlowContext(pipe,SimulationParameters.Default.copy(rotationDirection=RotationDirection.CounterClockwise)))
    val cw=force.acceleration(particle,FlowContext(pipe,SimulationParameters.Default.copy(rotationDirection=RotationDirection.Clockwise)))
    assert(ccw.dot(cw)<0)
  }
  test("centerline particle receives no swirl") { assert(new SwirlForce(new SolidBodySwirlProfile).acceleration(Particle(1,Vector3D(20,0,0),Vector3D.Zero),FlowContext(pipe,SimulationParameters.Default)).magnitude<1e-9) }
}
