package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector2D
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class TwistedPipeSpec extends AnyFunSuite {
  private val pipe=TwistedPipe(100,OvoidCrossSection(20,30,0.1),1.0)
  test("one turn reaches expected quarter/half/full angles") {
    assert(math.abs(pipe.rotationAngleAt(25)-math.Pi/2)<1e-9)
    assert(math.abs(pipe.rotationAngleAt(50)-math.Pi)<1e-9)
    assert(math.abs(pipe.rotationAngleAt(100)-2*math.Pi)<1e-9)
  }
  test("local-to-world transform is invertible") {
    val local=Vector2D(3,-4); val world=pipe.fromLocalCrossSection(37,local); val recovered=pipe.toLocalCrossSection(world)
    assert((recovered-local).magnitude<1e-9)
  }
  test("zero-turn twisted pipe behaves as untwisted coordinates") {
    val p=TwistedPipe(100,OvoidCrossSection(20,30,0.1),0); val local=Vector2D(2,5); assert(p.toLocalCrossSection(p.fromLocalCrossSection(60,local))==local)
  }
}
