# Schauberger Flow Visualization

Scala/ScalaFX particle visualization of helical, geometry-driven and secondary-flow concepts inspired by Viktor Schauberger's observations of water vortices.

> This is an educational and experimental visualization. It is not a Navier–Stokes CFD solver and does not claim to validate Schauberger's broader physical theories.

## Milestone 0.5.0 — Flow diagnostics and geometry performance pass

Milestone 0.5 builds on 0.4.0-buildfix1 and focuses on two goals:

1. calculate expensive local geometry data once per particle update,
2. expose cross-section velocity and vorticity diagnostics as heat maps.

The physical acceleration model remains:

```text
axial + swirl + secondary + wall
```

but force strategies now share a `ParticleGeometryContext`.

## Technology

- Scala 2.13.15
- ScalaFX 21
- JavaFX 21
- JDK 21
- Gradle 8.x
- ScalaTest + JUnitRunner
- GitHub Actions CI

## Default visualization

| Parameter | Default |
|---|---:|
| Particles | 750 |
| Axial velocity | 90.0 |
| Angular velocity | 0.55 |
| Swirl response | 2.0 |
| Secondary flow | enabled |
| Trail samples | 48 |
| Trail lifetime | 2.0 s |
| Heat map | enabled |
| Heat-map field | Velocity magnitude |
| Heat-map resolution | 30 x 30 |
| Secondary vectors | disabled by default |

All numeric sliders retain the milestone 0.4 behavior: each slider is paired with an editable numeric field. Values can be changed by dragging, typing and pressing Enter, or leaving the field. Dot/comma decimal separators are accepted and values are clamped to the configured range.

## Performance architecture

Milestone 0.4 revealed geometry as the dominant cost, especially for ovoid and twisted-ovoid cases. 0.5 therefore changes the per-particle pipeline to:

```text
Particle
  -> GeometryContextCalculator
  -> ParticleGeometryContext
       - localPosition
       - LocalFrame
       - signedBoundaryDistance
       - inwardNormal
       - twistAngle
       - twistRate
  -> ParticleFlowContext
  -> CompositeFlowForce
  -> Integrator
  -> BoundaryHandler
```

`AxialFlowForce`, `SwirlForce`, `SecondaryFlowForce` and `WallRepulsionForce` reuse the same local geometry context.

## Analytic ovoid normals

`OvoidCrossSection` no longer estimates its normal using finite differences. The boundary is differentiated analytically from its polar radius. This removes several repeated `signedDistance` evaluations and trigonometric calculations from near-wall particle updates.

`Rotation2D` additionally stores the already-calculated sine and cosine for a local twist angle.

## Flow diagnostics

Cross-section mode now supports the following heat-map fields:

- Velocity magnitude
- Axial velocity
- Tangential velocity
- Secondary/cross-sectional velocity
- Vorticity estimate

The diagnostic pipeline is:

```text
SimulationState
  -> CrossSectionVelocityFieldSampler
  -> VectorFieldGrid
  -> ScalarFieldCalculator
  -> ScalarFieldGrid
  -> HeatMapRenderer
```

Particle velocities are interpolated onto a regular local cross-section grid with a compact Gaussian kernel.

### Vorticity

For the cross-section velocity grid, the axial vorticity component is estimated as:

```text
omega_x = d(v_z)/d(y) - d(v_y)/d(z)
```

using centered finite differences. This is a cross-sectional diagnostic estimate, not a full three-dimensional CFD vorticity solution.

The implementation is validated by a solid-body rotation test where the analytic expectation is approximately `2 * omega`.

## Rendering layers

Cross-section rendering order is:

```text
background
heat map
secondary vector field (optional)
fading trails (optional)
particles (optional)
pipe boundary
legend
```

This keeps diagnostic fields readable without forcing every overlay to be enabled at the same time.

## Project structure

```text
src/main/scala/io/codeswarm/schauberger/
├── application/
├── benchmark/
├── diagnostics/
│   ├── FieldType.scala
│   ├── FieldRange.scala
│   ├── VectorFieldGrid.scala
│   ├── ScalarFieldGrid.scala
│   ├── CrossSectionVelocityFieldSampler.scala
│   ├── ScalarFieldCalculator.scala
│   └── VorticityFieldCalculator.scala
├── geometry/
│   ├── Rotation2D.scala
│   ├── LocalFrame.scala
│   ├── OvoidGeometryMath.scala
│   └── ...
├── math/
├── model/
├── physics/
├── simulation/
│   ├── ParticleGeometryContext.scala
│   ├── GeometryContextCalculator.scala
│   ├── ParticleFlowContext.scala
│   └── ...
├── ui/
│   ├── DiagnosticsControlPane.scala
│   ├── NumericSliderField.scala
│   └── ...
└── visualization/
    ├── HeatMapRenderer.scala
    └── ...
```

## Build and run

Requirements: JDK 21 and Gradle 8.x.

```bash
gradle clean test
gradle run
```

## Benchmark

```bash
gradle benchmark
```

The 0.5 benchmark adds:

- 5 warm-up iterations,
- 10 measurement iterations,
- median and p95 timings,
- particle updates/s,
- field-sampling measurements,
- a geometry-context microbenchmark for circular, ovoid and twisted-ovoid geometries.

Output:

```text
benchmark/results/benchmark-0.5.0.json
```

The benchmark is designed for regression tracking. Use JMH for publication-grade microbenchmarks.

## GitHub Actions

Two workflows are included:

- `.github/workflows/ci.yml` — checkout, JDK 21, Gradle 8.10.2, `clean test` on push/PR/manual runs.
- `.github/workflows/benchmark.yml` — manual benchmark run, tests first, then uploads `benchmark-0.5.0.json` as a workflow artifact.

## Tests

The suite covers the existing 0.1–0.4 behavior and new 0.5 functionality, including:

- precomputed geometry contexts,
- analytic ovoid normals,
- local/world frame round trips,
- force models using `ParticleFlowContext`,
- vorticity validation against solid-body rotation,
- scalar range normalization,
- long-running geometry containment,
- secondary flow,
- fading trails,
- editable numeric input parsing/clamping.

## Documentation

- `README.md` — user/developer overview
- `docs/ARCHITECTURE.md` — architecture and dependency boundaries
- `docs/IMPLEMENTATION_TASKS.md` — detailed implementation sequence and acceptance criteria
- `CHANGELOG.md` — milestone history

## License

Licensed under the **Apache License, Version 2.0**. See `LICENSE`.
