package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.StraightPipe
import io.codeswarm.schauberger.geometry.CircularCrossSection
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle,SimulationParameters}
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class AxialFlowForceSpec extends AnyFunSuite {
  test("force accelerates toward target axial velocity") {
    val c=FlowContext(StraightPipe(100,CircularCrossSection(10)),SimulationParameters.Default.copy(axialVelocity=100))
    val a=new AxialFlowForce.acceleration(Particle(1,Vector3D(10,0,0),Vector3D(20,0,0)),c)
    assert(a.x>0 && math.abs(a.y)<1e-9 && math.abs(a.z)<1e-9)
  }
}
