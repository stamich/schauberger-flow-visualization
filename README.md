# Schauberger Flow Visualization

Scala/ScalaFX particle visualization of helical, geometry-driven and secondary-flow concepts inspired by Viktor Schauberger's observations of water vortices.

> This is an educational and experimental visualization. It is not a Navier–Stokes CFD solver and does not claim to validate Schauberger's broader physical theories.

## Milestone 0.6.0 — Optimized Geometry Kernel & Vortex Profiles

Milestone 0.6 follows the benchmark conclusions from 0.5: ovoid geometry remains the dominant hotspot, while field sampling and secondary flow are already sufficiently inexpensive. The release therefore focuses on:

1. lookup-accelerated ovoid boundary evaluation with an exact reference mode,
2. selectable Solid Body, Rankine and Lamb-Oseen vortex profiles,
3. improved profile and geometry benchmarks, including JMH.

## Technology

- Scala 2.13.15
- ScalaFX 21.0.0-R32
- JavaFX / JDK 21
- Gradle
- ScalaTest + JUnitRunner
- JMH Gradle plugin 0.7.3
- GitHub Actions CI
- Apache License 2.0

## Defaults

| Parameter | Default |
|---|---:|
| Particles | 750 |
| Axial velocity | 90.0 |
| Angular velocity | 0.55 |
| Swirl response | 2.0 |
| Vortex profile | Solid Body |
| Secondary flow | enabled |
| Trail samples | 48 |
| Trail lifetime | 2.0 s |
| Heat-map resolution | 30 x 30 |
| Ovoid evaluation | Lookup |
| Ovoid lookup samples | 1024 |

All numeric sliders continue to use `NumericSliderField`, so every slider also has an editable numeric text field.

## Ovoid geometry modes

### Exact

`ExactOvoidGeometryKernel` evaluates the analytic polar ovoid radius and inward normal for every query. It is retained as the correctness reference.

### Lookup

`LookupOvoidGeometryKernel` uses an immutable `OvoidBoundaryLookup`. Geometry construction precomputes samples containing:

- angle,
- radius,
- radius derivative,
- inward unit normal.

Runtime queries use periodic index lookup and linear interpolation. The default table size is 1024 samples.

The UI exposes both the evaluation mode and the lookup sample count.

### Accuracy validation

The 0.6 benchmark compares lookup geometry with exact geometry over 10,000 validation angles and records:

- maximum relative radius error,
- mean relative radius error,
- maximum normal-angle error.

Tests require the default lookup resolution to stay below 0.1% radius error and 0.5 degrees normal-angle error.

## Vortex profiles

### Solid Body

```text
v_theta = omega * r
```

This preserves the previous milestone behavior and remains the default.

### Rankine

```text
v_theta(r) = omega * r                    r <= r_core
v_theta(r) = omega * r_core^2 / r         r >  r_core
```

The profile is continuous at the core boundary. `RankineParameters` stores the core radius as a fraction of the characteristic pipe radius.

### Lamb-Oseen

Milestone 0.6 adds a smooth finite-core Lamb-Oseen-inspired profile:

```text
v_theta ~ (1 - exp(-(r/a)^2)) / (r/a)
```

The circulation parameter is expressed in simulation units and is not presented as SI-calibrated circulation. The implementation is explicitly stable at `r = 0`.

## Profile architecture

```text
SwirlParameters.profile
       ↓
VortexProfileParameters
       ↓
SwirlProfileFactory
  ┌────┼──────────┐
  ↓    ↓          ↓
Solid Rankine Lamb-Oseen
       ↓
   SwirlForce
```

`SwirlProfileFactory` is the only ADT-to-strategy selection point. It caches strategies by immutable parameter value to avoid allocation in the particle hot path.

## Geometry architecture

```text
OvoidCrossSection
       ↓
OvoidGeometryKernel
   ┌───────┴────────┐
   ↓                ↓
Exact            Lookup
analytic      precomputed table
```

The rest of the physics still consumes `ParticleGeometryContext`, so force implementations remain independent of the concrete geometry kernel.

## Diagnostics

The 0.5 diagnostic stack is preserved:

- velocity magnitude heat map,
- axial velocity heat map,
- tangential velocity heat map,
- secondary velocity heat map,
- cross-sectional vorticity estimate,
- secondary vector field,
- fading trails,
- particle overlay.

0.6 adds `RadialProfileSampler` and `VortexProfileDiagnostics` for sampling analytic vortex profiles without running particles.

## Build and run

Requirements: JDK 21 and Gradle 8.x.

```bash
gradle clean test
gradle run
```

## Application benchmark

```bash
gradle benchmark
```

The benchmark performs 5 warm-up iterations and 10 measurement iterations and writes:

```text
benchmark/results/benchmark-0.6.0.json
```

It includes:

- Solid Body / Rankine / Lamb-Oseen simulation scenarios,
- ovoid and twisted-ovoid lookup scenarios,
- selected 50k stress cases,
- field-sampling measurements,
- exact-vs-lookup geometry-context throughput,
- lookup accuracy for 256 / 512 / 1024 / 2048 samples.

## JMH

```bash
gradle jmh
```

JMH microbenchmarks compare:

- exact and lookup ovoid geometry kernels,
- Solid Body, Rankine and Lamb-Oseen profile evaluation.

JMH complements, rather than replaces, the application benchmark.

## GitHub Actions

- `.github/workflows/ci.yml` runs `gradle --no-daemon clean test` on push, pull request and manual dispatch.
- `.github/workflows/benchmark.yml` is manually triggered. It runs tests, the application benchmark, JMH, and uploads `benchmark-0.6.0.json`.

## Test coverage added in 0.6

- lookup seam periodicity,
- interpolated normal normalization,
- exact-vs-lookup radius accuracy,
- exact-vs-lookup normal-angle accuracy,
- Rankine core continuity,
- Lamb-Oseen center stability,
- finite sampled Lamb-Oseen velocities,
- profile factory selection and caching,
- profile-sensitive `SwirlForce`,
- radial profile diagnostics,
- existing long-run containment and finite-vector invariants.

## Scientific scope

Particles are Lagrangian markers representing elements of a modeled fluid flow; they are not literal water molecules. Units are simulation units. The swirl and secondary-flow models are designed for visualization and controlled comparison, not as a substitute for validated CFD.

## Roadmap

The natural next milestone is 0.7: counter-rotating / double-vortex models and direct comparison between vortex profiles inside the same geometry.

## Documentation

- `README.md` — project and milestone overview
- `docs/ARCHITECTURE.md` — architecture and dependency boundaries
- `CHANGELOG.md` — milestone history

## License

Licensed under the **Apache License, Version 2.0**. See `LICENSE`.
