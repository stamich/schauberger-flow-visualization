package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector3D
/** Local orthonormal frame of the pipe cross-section at one axial position. */
final case class LocalFrame(tangent: Vector3D, normal: Vector3D, binormal: Vector3D, rotationAngle: Double)
