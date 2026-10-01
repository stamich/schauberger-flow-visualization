package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.{OvoidCrossSection,TwistedPipe}
import io.codeswarm.schauberger.model.SimulationParameters
import io.codeswarm.schauberger.physics.{AxialFlowForce,CompositeFlowForce,SolidBodySwirlProfile,SwirlForce,WallRepulsionForce}
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class SimulationEngineSpec extends AnyFunSuite {
  private val engine=new SimulationEngine(CompositeFlowForce(Vector(new AxialFlowForce,new SwirlForce(new SolidBodySwirlProfile),new WallRepulsionForce)),new SemiImplicitEulerIntegrator,new UniformCrossSectionParticleGenerator(42),new PipeBoundaryHandler)
  test("long run preserves population and geometry invariant") { val g=TwistedPipe(300,OvoidCrossSection(80,100,0.14),1.25);val p=SimulationParameters.Default.copy(particleCount=120);var s=engine.initialState(p,g);var i=0;while(i<2000){s=engine.step(s,p,g,p.fixedTimeStep);i+=1};assert(s.particles.size==120);assert(s.particles.forall(g.contains)) }
  test("swirl produces changing cross-section angle while x advances") { val g=TwistedPipe(300,OvoidCrossSection(80,100,0.1),1);val p=SimulationParameters.Default.copy(particleCount=1);val s0=engine.initialState(p,g);var s=s0;var i=0;while(i<300){s=engine.step(s,p,g,p.fixedTimeStep);i+=1};val a0=math.atan2(s0.particles.head.position.z,s0.particles.head.position.y);val a1=math.atan2(s.particles.head.position.z,s.particles.head.position.y);assert(s.particles.head.position.x!=s0.particles.head.position.x);assert(math.abs(a1-a0)>1e-3) }
}
