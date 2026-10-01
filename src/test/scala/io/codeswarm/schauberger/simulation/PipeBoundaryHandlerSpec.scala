package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.{OvoidCrossSection,TwistedPipe}
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle,SimulationParameters}
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class PipeBoundaryHandlerSpec extends AnyFunSuite {
  test("outside particle is clamped into arbitrary geometry") { val g=TwistedPipe(100,OvoidCrossSection(20,30,0.1),1);val gen=new UniformCrossSectionParticleGenerator(1);val p=Particle(1,Vector3D(50,50,50),Vector3D(0,20,20));val fixed=(new PipeBoundaryHandler).handle(p,g,SimulationParameters.Default,gen);assert(g.contains(fixed.position)) }
  test("outlet particle is respawned at inlet") { val g=TwistedPipe(100,OvoidCrossSection(20,30,0.1),1);val fixed=(new PipeBoundaryHandler).handle(Particle(1,Vector3D(101,0,0),Vector3D.Zero),g,SimulationParameters.Default,new UniformCrossSectionParticleGenerator(1));assert(fixed.position.x==0.0) }
}
