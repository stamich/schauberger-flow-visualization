# Schauberger Flow Visualization

Scala/ScalaFX particle visualization of helical and geometry-driven flow concepts inspired by Viktor Schauberger's observations of water vortices.

> This project is an educational and experimental visualization. It does **not** claim to validate Schauberger's broader physical theories and it is not a Navier-Stokes CFD solver.

## Milestone 0.3.0-buildfix1

> **Buildfix1:** fixes a ScalaFX compile-time name collision in `ControlPanel`: slider fields formerly named `width` and `height` conflicted with inherited `VBox.width` / `VBox.height` properties. They are now named `widthSlider` and `heightSlider`.

Milestone 0.3 generalizes the pipe from a circular cylinder to geometry-driven flow. The same simulation engine now supports:

- circular cross-sections,
- asymmetric ovoid cross-sections,
- ovoid cross-sections rotating continuously along the pipe axis,
- geometry-aware wall repulsion and hard boundary handling,
- local-frame swirl,
- longitudinal and physical cross-section views,
- short, fading particle trails,
- geometry performance benchmarks with JSON output.

### New readable defaults

The default population has been reduced from **1500 to 750 particles**. The initial flow was retuned at the same time:

| Parameter | 0.2 default | 0.3 default |
|---|---:|---:|
| Particles | 1500 | **750** |
| Axial velocity | 120.0 | **90.0** |
| Angular velocity | 0.90 | **0.55** |
| Swirl response | 3.0 | **2.0** |
| Trail length | 60 | **48 samples** |
| Trail lifetime | unlimited except sample cap | **2.0 s** |
| Trail sampling | every physics frame | **every 4 frames** |

These values intentionally favor visual clarity over density.

## Fading trails

Milestone 0.2 kept a bounded number of points, but hundreds of particles still produced a dense mesh after a few seconds. 0.3 stores timestamped `TrailSample` values and applies two independent limits:

1. `trailLength` caps samples per particle.
2. `trailDurationSeconds` expires old samples by simulation age.

Opacity decreases linearly with sample age:

\[
\alpha = \alpha_0 \max\left(0, 1 - \frac{age}{T_{trail}}\right)
\]

With defaults of 48 points, 2 seconds, and sampling every 4 physics frames, trails remain long enough to reveal a helix but do not accumulate indefinitely.

## Geometry model

### Circular

The baseline circular cross-section remains available for comparison.

### Ovoid

`OvoidCrossSection` starts from an ellipse and modulates the polar boundary radius:

\[
R(\phi) = R_{ellipse}(\phi)\left(1 + a\sin\phi\right)
\]

where `a` is the asymmetry parameter.

### Twisted ovoid

`TwistedPipe` rotates the local cross-section around the x axis:

\[
\theta(x) = 2\pi N\frac{x}{L}
\]

where `N` is the number of turns and `L` is pipe length. Physics always works in the local frame; forces therefore contain no shape-specific branching.

## Flow model

Axial flow is controlled as a target velocity:

\[
a_x = k_x(v_{target} - v_x)
\]

The solid-body swirl profile remains:

\[
v_\theta = \omega r
\]

and `SwirlForce` approaches that target with a first-order response. `WallRepulsionForce` now uses `PipeGeometry.signedDistanceToBoundary` and `PipeGeometry.inwardNormal` rather than circular radius assumptions.

## Cross-section view

The cross-section view now represents a physical axial slice. Only particles within `crossSectionSliceHalfWidth` of the selected x position are rendered. The `Cross-section x` slider selects the slice from inlet (0%) to outlet (100%).

## Project structure

```text
src/main/scala/io/codeswarm/schauberger/
├── application/
├── benchmark/
├── geometry/
│   ├── CrossSectionShape.scala
│   ├── CircularCrossSection.scala
│   ├── OvoidCrossSection.scala
│   ├── LocalFrame.scala
│   ├── PipeGeometry.scala
│   ├── StraightPipe.scala
│   ├── StraightCircularPipe.scala
│   ├── TwistedPipe.scala
│   └── GeometryFactory.scala
├── math/
├── model/
│   ├── SimulationParameters.scala
│   ├── GeometryParameters.scala
│   └── VisualizationParameters.scala
├── physics/
├── simulation/
├── ui/
└── visualization/
```

## Architectural rule

Dependencies point toward domain abstractions:

```text
ScalaFX UI
   ↓
SimulationController
   ↓
SimulationEngine ───── PipeGeometry
   ↓                     ↓
FlowForce          CrossSectionShape
   ↓
math / model
```

No physics class checks `Circular`, `Ovoid`, or `TwistedOvoid` with `if`/`match`.

## Build and run

Requirements:

- JDK 21
- Gradle

```bash
gradle clean test
gradle run
```

or:

```bash
./scripts/run.sh
```

## Benchmark

```bash
gradle benchmark
```

Scenarios:

- `circular-axial`
- `circular-swirl`
- `ovoid-swirl`
- `twisted-ovoid-swirl`

Population sizes: 1,000 / 10,000 / 50,000 particles.

Results are saved to:

```text
benchmark/results/benchmark-0.3.0.json
```

This is a lightweight regression benchmark, not a replacement for JMH.

## Tests

The suite covers vector mathematics, cross-section contracts, ovoid asymmetry, twist transforms, geometry factory, default settings, flow forces, arbitrary-shape particle generation, boundary invariants, metrics, projections, fading/expiring trails, and long-running engine invariants.

## Roadmap

- **0.1** axial particle-flow foundation
- **0.2** 3D swirl and helical trajectories
- **0.3** geometry-driven circular / ovoid / twisted-ovoid flow
- **0.4** geometry-induced secondary flow and Dean-like vortices
- **0.5** field sampling / heat maps and more physical diagnostics
- later: more advanced vortex profiles and comparison with CFD reference cases
