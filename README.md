# Schauberger Flow Visualization

An educational and experimental particle-flow visualization inspired by Viktor Schauberger's observations of water motion, implemented in Scala and ScalaFX.

> **Scientific scope:** this project does not claim to validate Schauberger's broader physical theories. Milestone 0.1 models a conventional particle flow through a straight pipe and establishes a clean baseline for later vortex-flow experiments.

## Milestone 0.1

The first milestone focuses on the simulation foundation rather than spectacular visuals. It provides:

- particles moving through a straight pipe,
- target axial flow velocity,
- wall confinement,
- continuous outlet-to-inlet respawn,
- immutable simulation state,
- semi-implicit Euler integration,
- fixed simulation timestep,
- Canvas-based ScalaFX rendering,
- interactive controls for particle count and flow velocity,
- unit/integration tests and a lightweight benchmark.

The flow model for 0.1 is intentionally simple:

```text
velocity = axial component only

v_theta = 0
v_radial = 0
```

Milestone 0.2 will add the first explicit vortex component.

## Requirements

- JDK 21 recommended
- Gradle 8.x (or the Gradle wrapper if added by the repository owner)
- Linux, macOS, or Windows with JavaFX support

## Run

```bash
./gradlew run
```

If no wrapper is present yet:

```bash
gradle run
```

## Test

```bash
./gradlew clean test
```

## Benchmark

The benchmark intentionally excludes rendering and measures only the simulation engine:

```bash
./gradlew benchmark
```

It runs sample workloads for 1,000, 10,000, and 50,000 particles.

## Architecture

The key rule is that ScalaFX is a presentation layer only. Physics and simulation code do not depend on UI classes.

```text
UI / ScalaFX
     |
     v
SimulationController
     |
     v
SimulationEngine
  /    |      \
forces integrator boundaries
  \    |      /
   SimulationState
        |
        v
    Renderer
```

Package responsibilities:

- `math` - vector mathematics.
- `model` - immutable domain state and parameters.
- `geometry` - pipe geometry abstractions.
- `physics` - force calculations.
- `simulation` - particle generation, integration, boundaries, and stepping.
- `visualization` - Canvas rendering and coordinate mapping.
- `ui` - controls and animation loop.
- `application` - application bootstrap only.
- `benchmark` - headless performance smoke benchmark.

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the detailed design and [docs/IMPLEMENTATION_TASKS.md](docs/IMPLEMENTATION_TASKS.md) for the implementation order.

## Controls

- **Start** - starts/resumes simulation.
- **Pause** - pauses simulation without destroying state.
- **Reset** - creates a fresh deterministic particle distribution using current controls.
- **Particles** - chooses the number of particles. A change is applied on reset.
- **Axial velocity** - changes the target horizontal flow velocity live.

## Mathematical model

For particle `i`, the total acceleration is the sum of configured forces:

```text
a_i = a_axial + a_wall
```

Axial force drives the x velocity toward a target rather than accelerating indefinitely:

```text
a_x = response * (targetVelocity - v_x)
```

The wall force activates near the upper and lower boundaries and points toward the pipe interior.

The integrator is semi-implicit Euler:

```text
v(t + dt) = v(t) + a(t) * dt
x(t + dt) = x(t) + v(t + dt) * dt
```

## Roadmap

- **0.2** - swirl/vortex force and helical-looking 2D trajectory projection.
- **0.3** - alternative pipe geometries and stronger wall/geometry interaction.
- **0.4** - ovoid/twisted geometry model.
- **0.5** - trajectory trails and cross-section visualization.
- **0.6** - vector field and heat-map rendering.
- **0.7** - local viscosity-like interaction.
- **0.8** - counter-rotating / double-vortex experimental model.
- **1.0** - side-by-side baseline vs Schauberger-inspired comparison.

## License

No license is imposed by this milestone package. Add the license appropriate for the target repository before publishing.
