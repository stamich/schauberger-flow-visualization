# Schauberger Flow Visualization

`0.2.0` is an educational Scala/ScalaFX particle visualization of helical flow concepts inspired by Viktor Schauberger's observations of vortex-like water motion. The project is deliberately explicit about the boundary between **modern vector/flow modelling** and broader historical claims that are not established by contemporary fluid mechanics.

## Milestone 0.2 goal

Milestone 0.1 established a clean particle engine with axial flow. Milestone 0.2 adds the first visually characteristic behavior:

```text
axial flow + controlled tangential swirl -> helical tracer trajectories
```

The simulation is still intentionally lightweight: it is **not** a Navier-Stokes CFD solver and does not calculate pressure, density or turbulence fields.

## Technology

- Scala 2.13.15
- ScalaFX / JavaFX 21
- Gradle
- ScalaTest 3.2.19
- Java 21 recommended

## Mathematical model

The pipe axis is the x axis. Each particle has 3D position and velocity:

```text
position = (x, y, z)
velocity = (vx, vy, vz)
```

### Axial response

The axial force approaches a target velocity rather than applying constant acceleration:

```text
a_x = k_a * (v_target - v_x)
```

### Solid-body swirl

Milestone 0.2 uses a `SolidBodySwirlProfile`:

```text
v_theta,target = omega * r
```

`SwirlForce` derives a local tangential direction from the pipe tangent and radial vector, then applies a first-order response:

```text
a_theta = k_s * (v_theta,target - v_theta,current)
```

This prevents unbounded angular acceleration and keeps the profile replaceable in future milestones.

### Soft wall confinement

Near the cylindrical wall a soft radial force points toward the center. `PipeBoundaryHandler` remains the hard numerical safety layer.

### Integration

Particles use semi-implicit Euler with a fixed physics timestep (default 1/120 s).

## Views

### Longitudinal

Projects `(x, y, z)` to `(x, y)`. A 3D helix appears as an oscillating path in the side view.

### Cross section

Projects `(x, y, z)` to `(y, z)`, exposing the circular swirl directly.

Use the View selector to switch between both without changing physical state.

## Controls

- Start / Pause / Reset
- particle count (applied on Reset)
- axial velocity
- angular velocity
- swirl response
- trail length
- clockwise / counter-clockwise rotation
- longitudinal / cross-section view

The status row displays FPS, mean axial velocity, signed mean tangential velocity, signed mean angular velocity and a **vorticity proxy**. The proxy is `2 * meanAngularVelocity`, appropriate as a diagnostic for the current solid-body model; it is not a grid-computed curl field.

## Architecture

```text
ScalaFX UI
   |
SimulationController
   |
SimulationEngine
   +-- CompositeFlowForce
   |    +-- AxialFlowForce
   |    +-- SwirlForce -> SwirlProfile
   |    +-- WallRepulsionForce
   +-- ParticleIntegrator
   +-- BoundaryHandler
   +-- PipeGeometry

SimulationState -> Projection -> Canvas renderer
                    |-- LongitudinalProjection
                    `-- CrossSectionProjection
```

Physical state is immutable. `TrailBuffer` is intentionally mutable but lives exclusively in the visualization layer.

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for details.

## Build and run

If Gradle is installed:

```bash
gradle clean test
gradle run
```

If a Gradle wrapper is added locally, the same commands can be executed with `./gradlew`.

A convenience launcher is available:

```bash
./scripts/run.sh
```

## Benchmark

The lightweight headless benchmark compares:

- `axial-only`
- `axial-plus-swirl`

for 1,000, 10,000 and 50,000 particles.

```bash
gradle benchmark
```

It prints CSV-compatible output and writes:

```text
benchmark/results/benchmark-0.2.0.json
```

This benchmark is intended for milestone regression tracking, not as a replacement for JMH.

## Tests

Tests cover:

- 3D vector algebra and cross product
- circular 3D pipe geometry
- axial, wall and swirl forces
- solid-body swirl profile
- numerical integration
- deterministic particle generation
- hard boundary handling
- flow metrics
- projections and viewport mapping
- full engine invariants
- emergence of cross-section motion and a changing helical angle

## Current limitations

- no pressure or density field
- no Navier-Stokes solver
- no turbulence model
- no particle-particle hydrodynamic interaction
- only straight circular geometry
- only solid-body swirl profile
- trails are a visualization history, not physical pathline integration independent of rendering cadence

## Roadmap

- **0.1** — axial particle engine
- **0.2** — 3D swirl, helical trajectories, trails, cross-section view
- **0.3** — ovoid geometry and geometry-dependent flow
- **0.4** — twisted ovoid geometry
- **0.5** — richer trajectory/cross-section diagnostics
- **0.6** — field sampling, vector field and heat maps
- **0.7** — local viscosity / particle interaction model
- **0.8** — counter-rotating / double-vortex experiments
- **1.0** — controlled comparison of conventional and Schauberger-inspired flow configurations

## Scientific scope

This software uses standard geometry, vector algebra and deliberately simplified flow rules to explore vortex-shaped tracer motion. It should not be interpreted as experimental validation of Schauberger's broader claims about implosion, energy generation or biological properties of water.
