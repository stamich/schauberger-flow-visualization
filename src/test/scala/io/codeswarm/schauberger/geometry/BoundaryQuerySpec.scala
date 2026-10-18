package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector2D
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BoundaryQuerySpec extends AnyFunSuite {
  private val points = for {
    x <- Seq(-90.0, -20.0, 0.0, 7.5, 60.0)
    y <- Seq(-100.0, -5.0, 0.0, 33.0, 80.0)
  } yield Vector2D(x, y)

  private def agrees(shape: CrossSectionShape): Unit =
    points.foreach { point =>
      val query = shape.boundaryQuery(point)
      assert(query.signedDistance == shape.signedDistance(point), s"distance at $point")
      assert(query.inwardNormal == shape.inwardNormal(point), s"normal at $point")
    }

  test("ovoid lookup answers one query exactly like two separate calls") {
    agrees(OvoidCrossSection(220.0, 250.0, 0.14, GeometryEvaluationMode.Lookup, 512))
  }

  test("ovoid exact mode answers one query exactly like two separate calls") {
    agrees(OvoidCrossSection(220.0, 250.0, 0.14, GeometryEvaluationMode.Exact))
  }

  test("circular section uses the default combination") {
    agrees(CircularCrossSection(100.0))
  }

  test("the axis has distance equal to the boundary radius and a zero normal") {
    val shape = OvoidCrossSection(220.0, 250.0, 0.14)
    val query = shape.boundaryQuery(Vector2D.Zero)
    assert(query.inwardNormal == Vector2D.Zero)
    assert(query.signedDistance > 100.0)
  }

  test("ovoid sections default to a 512-sample lookup") {
    assert(OvoidCrossSection(220.0, 250.0, 0.14).lookupSamples == 512)
    assert(io.codeswarm.schauberger.model.GeometryParameters.Default.lookupSamples == 512)
  }
}
