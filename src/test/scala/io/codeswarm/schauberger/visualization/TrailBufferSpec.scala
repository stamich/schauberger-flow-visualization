package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle,SimulationState}
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class TrailBufferSpec extends AnyFunSuite {
  test("trail buffer expires samples by age and length") { val b=new TrailBuffer;var f=0L;while(f<20){b.record(SimulationState(Vector(Particle(1,Vector3D(f.toDouble,0,0),Vector3D.Zero)),f.toDouble * 0.1,f),5,0.35,1);f+=1};assert(b.points(1).size<=4);assert(b.points(1).size<=5) }
  test("sampling interval reduces stored points") { val b=new TrailBuffer;(1L to 12L).foreach(f=>b.record(SimulationState(Vector(Particle(1,Vector3D(f.toDouble,0,0),Vector3D.Zero)),f.toDouble / 120.0,f),100,10,4));assert(b.points(1).size==3) }
}
