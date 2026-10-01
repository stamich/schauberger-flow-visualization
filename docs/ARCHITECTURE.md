# Architecture - Milestone 0.1

## Goals

Milestone 0.1 establishes an extensible simulation kernel for later vortex-flow experiments. The central design objective is to keep mathematical/physical code independent from ScalaFX.

## Dependency direction

```text
application
    |
    v
ui -------------> visualization
 |                      |
 v                      v
simulation ----------> model
 |   |                   ^
 |   +---- geometry -----+
 |   +---- physics ------+
 |
 +-------- math
```

Lower-level packages never depend on `ui` or `application`.

## Core data flow

```text
SimulationParameters
       |
       v
SimulationEngine.step(state, parameters, dt)
       |
       +--> FlowForce acceleration
       +--> ParticleIntegrator
       +--> speed limiting
       +--> BoundaryHandler
       |
       v
new SimulationState
       |
       v
SimulationRenderer
       |
       v
ScalaFX Canvas
```

## Immutability

`Particle`, `SimulationState`, `SimulationParameters`, `Vector2D`, and geometry values are immutable case classes. The animation controller owns only the reference to the current immutable state.

This gives deterministic, testable simulation functions while keeping mutation limited to the JavaFX animation lifecycle.

## Fixed timestep

Rendering rate and simulation update rate are intentionally separate. The animation timer accumulates wall-clock time and executes one or more simulation updates using a constant timestep (`1/120 s` by default).

Benefits:

- consistent simulation independent of monitor refresh rate,
- better reproducibility,
- protection from occasional slow render frames,
- easier future comparison of force models.

## Force model

`FlowForce` is an extension point. `CompositeFlowForce` sums all force contributions.

Milestone 0.1:

```text
CompositeFlowForce
  |- AxialFlowForce
  `- WallRepulsionForce
```

Milestone 0.2 can add `SwirlForce` without changing `SimulationEngine`.

## Geometry model

`PipeGeometry` defines the minimal contract required by physics and boundaries. In 0.1, `StraightCircularPipe` is rendered as a longitudinal 2D section of a cylindrical pipe.

Future implementations can introduce ovoid and twisted geometry while retaining the same simulation interfaces.

## Boundary semantics

Particles crossing the outlet are respawned at the inlet with the same ID. This models continuous flow while keeping particle count constant. Positions outside the top/bottom walls are clamped and the outward velocity component is removed.

## Rendering model

A single ScalaFX `Canvas` is used instead of thousands of scene-graph nodes. This is important for scaling to tens of thousands of particles in later milestones.

`ViewportTransform` separates world/simulation coordinates from screen pixels.

## Deliberate non-goals for 0.1

- Navier-Stokes solver,
- pressure field solution,
- real SI-unit calibration,
- turbulence model,
- particle-particle collisions,
- vortex/swirl force,
- 3D rendering,
- claims about Schauberger's speculative physics.
