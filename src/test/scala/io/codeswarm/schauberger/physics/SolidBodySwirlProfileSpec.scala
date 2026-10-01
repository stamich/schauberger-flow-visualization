package io.codeswarm.schauberger.physics

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class SolidBodySwirlProfileSpec extends AnyFunSuite {
  test("tangential target scales linearly with radius") { val p=new SolidBodySwirlProfile; assert(p.tangentialVelocity(2,10,0.5)==1.0);assert(p.tangentialVelocity(4,10,0.5)==2.0) }
}
