package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.model.{GeometryParameters,GeometryType}
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class GeometryFactorySpec extends AnyFunSuite {
  private val factory=new GeometryFactory

  test("factory creates all milestone geometries") {
    val base=GeometryParameters.Default
    assert(factory.create(base.copy(geometryType=GeometryType.Circular)).isInstanceOf[StraightPipe])
    assert(factory.create(base.copy(geometryType=GeometryType.Ovoid)).isInstanceOf[StraightPipe])
    assert(factory.create(base.copy(geometryType=GeometryType.TwistedOvoid)).isInstanceOf[TwistedPipe])
  }
}
