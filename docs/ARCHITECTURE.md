# Architecture — milestone 0.2

## Design goals

Milestone 0.2 keeps the 0.1 separation between simulation and ScalaFX while upgrading the physical coordinate model from 2D to 3D. The core rules are:

1. `SimulationEngine` contains no UI dependencies.
2. Physical state is immutable.
3. Forces are composable strategies.
4. Swirl behavior is split into `SwirlForce` and a replaceable `SwirlProfile`.
5. Rendering consumes projections instead of teaching physics about screen coordinates.
6. Mutable history is confined to the renderer (`TrailBuffer`).

## Dependency direction

```text
application
   |
   v
ui --------------------> visualization
   |                           |
   v                           v
simulation ----------------> model/math
   |
   +----> physics ----------> geometry/model/math
   `----> geometry/model/math
```

There is no dependency from `physics`, `simulation`, `geometry`, `model` or `math` to ScalaFX.

## Simulation pipeline

```text
Particle
   |
   v
FlowContext
   |
   v
CompositeFlowForce
   +-- AxialFlowForce
   +-- SwirlForce
   |      `-- SwirlProfile
   |             `-- SolidBodySwirlProfile
   `-- WallRepulsionForce
   |
   v
SemiImplicitEulerIntegrator
   |
   v
PipeBoundaryHandler
   |
   v
new Particle
```

The complete population forms a new immutable `SimulationState` for every physics step.

## 3D convention

- x: pipe axis / inlet-to-outlet direction
- y: first cross-section coordinate
- z: second cross-section coordinate

For a straight pipe the center-line tangent is `(1, 0, 0)` and radial distance is `sqrt(y^2 + z^2)`.

## Swirl model

`SwirlForce` does not hard-code a vortex profile. `SwirlProfile` returns the target tangential speed at a radius.

For 0.2:

```text
SolidBodySwirlProfile:
    targetTangentialSpeed = angularVelocity * radius
```

The force computes:

```text
radial = position - centerLine(position.x)
tangentialDirection = tangent cross radial
currentTangentialSpeed = velocity dot tangentialDirection
acceleration = tangentialDirection * swirlResponse * (target - current)
```

Rotation direction is applied as a sign to `tangentialDirection`.

This makes 0.3+ profiles such as Rankine or Lamb-Oseen additive rather than invasive.

## Boundary policy

`WallRepulsionForce` is soft confinement. `PipeBoundaryHandler` is hard safety:

- x > pipe length -> respawn at inlet
- radial position outside wall -> clamp just inside wall
- outward normal velocity after clamp -> removed

## Fixed timestep

`SimulationController` accumulates display time and advances the engine in `fixedTimeStep` increments. Rendering cadence therefore does not directly determine physics step size.

## Rendering

3D physics is projected into 2D:

```text
LongitudinalProjection: (x, y, z) -> (x, y)
CrossSectionProjection: (x, y, z) -> (y, z)
```

`ViewportTransform` then maps projected world coordinates to Canvas pixels.

`TrailBuffer` stores recent positions by particle id. It is intentionally mutable because it is ephemeral rendering state, not simulation state.

## Metrics

`FlowMetricsCalculator` derives:

- mean axial velocity
- signed mean tangential velocity
- signed mean angular velocity `v_theta / r`
- solid-body vorticity proxy `2 * mean angular velocity`

Metrics do not feed back into physics.

## Extension points for milestone 0.3

The following abstractions should remain stable:

- `SimulationEngine`
- `ParticleIntegrator`
- `FlowForce`
- `SwirlProfile`
- `Projection`

Milestone 0.3 should primarily extend `PipeGeometry` and adjust geometry-aware wall/radial calculations rather than rewrite the engine.
