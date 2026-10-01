package io.codeswarm.schauberger.physics

/** Defines target tangential speed as a function of radial distance and geometry scale. */
trait SwirlProfile { def tangentialVelocity(radialDistance: Double, characteristicRadius: Double, angularVelocity: Double): Double }
