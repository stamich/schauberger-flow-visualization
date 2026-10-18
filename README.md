# Schauberger Flow Visualization

Scala/ScalaFX particle visualization of helical, geometry-driven and secondary-flow concepts inspired by Viktor Schauberger's observations of water vortices.

> This is an educational and experimental visualization. It is not a Navier–Stokes CFD solver and does not claim to validate Schauberger's broader physical theories.

## Milestone 0.7.0 — Counter-Rotating Vortex

Milestone 0.7 adds a **counter-rotating (double) vortex**: a core that turns with the configured direction surrounded by
a layer that turns against it, joined by a smooth transition. Analysing milestones 0.1–0.6 for this release also found
four defects in the existing force model that a reversing profile would have made obvious, so they are fixed here:

1. Swirling tracers drifted toward the wall because nothing supplied the centripetal acceleration (`CentripetalBalanceForce`).
2. Swirl and geometry-induced secondary flow each relaxed the whole in-plane velocity and halved each other
   (`CrossSectionFlowForce`).
3. Some tests could not fail, because the boundary handler clamps every particle; they now assert on radial occupancy
   and angular velocity.
4. `SwirlProfileFactory` cached every parameter set ever seen.

See `CHANGELOG.md` for the full list.

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
| Ovoid lookup samples | 512 |
| Centripetal balance | enabled, strength 1.0 |
| Heat-map source | Particles, smoothing 0.7 |
| Colour by rotation sense | on |

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

Runtime queries use periodic index lookup and linear interpolation over primitive arrays. The default table size is 512 samples; measured against the exact kernel it gives a maximum relative radius error of 7.8e-6 and a normal error of 6.4e-4 degrees. The geometry context asks the cross-section for distance and normal in one `boundaryQuery`, so the polar angle and the lookup are evaluated once per particle.

The UI exposes both the evaluation mode and the lookup sample count.

### Accuracy validation

The benchmark compares lookup geometry with exact geometry over 10,000 validation angles and records:

- maximum relative radius error,
- mean relative radius error,
- maximum normal-angle error.

Tests require the 512-sample default to stay below 0.002% radius error and 0.002 degrees normal-angle error, and that one boundary query equals the two separate calls.

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

### Counter-rotating

```text
v_theta(r) = s_i * w(rho) * v_inner(r)  -  s_o * (1 - w(rho)) * v_outer(r)
w(rho)     = 1/2 * (1 - tanh((rho - r_t) / delta)),     rho = r / R
```

`CounterRotatingParameters` holds the two layers (any single-vortex profile), their strengths `s_i` and `s_o`, the
transition radius `r_t` and the transition width `delta`. Positive velocity turns with the configured rotation direction,
negative against it; `SwirlProfile.tangentialVelocity` is a **signed** contract.

Things worth knowing when tuning it:

- The zero crossing lies at `r_t` only when both contributions are equal there. With unequal strengths or layer shapes
  it moves toward the weaker layer.
- The default outer layer is solid-body. Two Rankine layers would make the outer region almost irrotational, so the
  reversal would be barely visible.
- The transition uses `rho = r / characteristicRadius`, which is exact for a circular section and an approximation for
  the ovoid.

The UI offers four presets (*Core and shell*, *Strong reverse shell*, *Gentle reversal*, *Thin shear layer*).

## Profile architecture

```text
SwirlParameters.profile
       ↓
VortexProfileParameters
   ├── SimpleVortexProfileParameters
   │      SolidBody · Rankine · LambOseen
   └── CounterRotatingParameters (two simple layers)
       ↓
SwirlProfileFactory
  ┌────┼──────────┬───────────────────────┐
  ↓    ↓          ↓                       ↓
Solid Rankine Lamb-Oseen   CounterRotatingVortexProfile
                                 (TanhTransition)
       ↓
CrossSectionFlowForce
```

`SwirlProfileFactory` is the only ADT-to-strategy selection point. It remembers the most recently requested parameters, so the hot path is one reference comparison and dragging a slider cannot grow a cache.

## Forces

`DefaultFlowForces` assembles the stack used by the application, tests and benchmarks:

| Force | Role |
|---|---|
| `AxialFlowForce` | relaxes the axial velocity toward the drive velocity |
| `CrossSectionFlowForce` | relaxes the in-plane velocity toward profile swirl plus the secondary-flow target, once per axis |
| `CentripetalBalanceForce` | supplies `-v_theta^2 / r` toward the axis so a swirling tracer stays on its orbit |
| `WallRepulsionForce` | pushes tracers away from the boundary |

Why one cross-section force: swirl and the twin-vortex secondary field both act in the cross-section. As two independent
relaxations they pulled the same tangential component toward different targets, so swirl settled at `k_s / (k_s + k_2)` of
its profile value. `CrossSectionFlowForce` resolves the velocity on the local radial and tangential axes and relaxes each
once; the secondary target is added to the swirl target instead of competing with it.

Why the balance: in a real vortex the radial pressure gradient supplies the centripetal acceleration. The tracers carry
no pressure, so without this term they move along straight tangents and drift outward. The radius used is floored at 5%
of the characteristic radius so a tracer near the axis cannot receive an unbounded acceleration.

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

0.6 added `RadialProfileSampler` and `VortexProfileDiagnostics` for sampling analytic vortex profiles without running particles.

0.7 makes the diagnostics sign-aware, because a signed mean angular velocity cancels for a counter-rotating vortex:

- **Region metrics.** Each tracer is *forward*, *reverse* or *quiescent* (below 5% of the configured angular velocity).
  The status bar shows the three shares; `FlowMetrics.regions` also holds the mean forward, reverse and absolute angular
  velocities.
- **Radial occupancy.** The share of tracers in the inner, middle and outer third of the normalized radius. An
  area-uniform population gives about 11 / 33 / 56 %; a large departure means inward or outward drift.
- **Signed profile sampling.** `RadialProfileSampler` reports the peak by magnitude, plus the zero crossings
  (`ZeroCrossingDetector`) and the most negative velocity.
- **Diverging palette.** Tangential velocity and vorticity are drawn on a range centred on zero (red = counter-clockwise, blue = clockwise,
  neutral = no rotation), so the zero crossing shows as a neutral band.
- **Analytic reference.** The heat map source can be switched from the particles to the analytic profile, which is
  noise-free. Particle fields are smoothed with an exponential moving average (default memory 0.7) that restarts when the
  field, source or resolution changes.
- **Colour by rotation sense.** Particles and trails are orange (with the direction), light blue (against) or grey
  (quiescent), using the same rule as the status-bar shares.

## User interface

```text
ControlPanel
├── FlowControlPane            particles, axial/swirl, secondary flow, centripetal balance, direction
├── VortexProfileControlPane   profile kind, profile sliders, counter-rotating sliders and presets
├── GeometryControlPane
├── VisualizationControlPane   view, trails, layers, colour by rotation sense
└── DiagnosticsControlPane     heat-map field, source, grid, smoothing
```

The switching and slider rules of the profile pane are in `VortexProfileEdits`, which has no ScalaFX dependency and is unit tested.

## Build and run

Requirements: JDK 21. The Gradle wrapper (8.10.2, checksum-pinned) is included; on Windows use `gradlew.bat`.

```bash
./gradlew clean test
./gradlew run
```

## Application benchmark

```bash
./gradlew benchmark
```

The benchmark performs 5 warm-up iterations and 10 measurement iterations and writes:

```text
benchmark/results/benchmark-0.7.0.json
```

It includes:

- Solid Body / Rankine / Lamb-Oseen simulation scenarios,
- ovoid and twisted-ovoid lookup scenarios (512-sample default),
- counter-rotating scenarios with and without secondary flow, and a Rankine scenario with the balance off,
- selected 50k stress cases,
- field-sampling measurements,
- exact-vs-lookup geometry-context throughput (512 and 1024 samples),
- lookup accuracy for 256 / 512 / 1024 / 2048 samples.

`docs/BENCHMARKS.md` compares 0.6 and 0.7 and explains how to read the numbers.

## JMH

```bash
./gradlew jmh
```

JMH microbenchmarks compare:

- exact and lookup ovoid geometry kernels, including the single-ray lookup and the combined versus separate cross-section query,
- Solid Body, Rankine, Lamb-Oseen and counter-rotating profile evaluation.

JMH complements, rather than replaces, the application benchmark.

## GitHub Actions

- `.github/workflows/ci.yml` runs `./gradlew --no-daemon clean test` on push, pull request and manual dispatch.
- `.github/workflows/benchmark.yml` is manually triggered. It runs tests, the application benchmark, JMH, and uploads `benchmark-0.7.0.json` and `jmh-0.7.0.json`.

## Test coverage added in 0.7

- Rankine vorticity (`2 omega` in the core, zero outside) and constant outer circulation,
- `TanhTransition` bounds, symmetry and monotonicity,
- counter-rotating profile sign pattern, continuity and the movement of the zero crossing with unequal strengths,
- factory construction and caching of composites,
- centripetal balance direction, magnitude, floor and switching,
- `CrossSectionFlowForce`: no axial component, secondary component independent of rotation direction, equilibrium,
  swirl no longer halved by secondary flow,
- radial occupancy of uniform populations and engine runs; with the balance the distribution stays at its no-swirl level
  and without it the outer third overfills,
- region classification and metrics, including the sign convention for clockwise rotation,
- zero-crossing detection and signed profile sampling,
- analytic vorticity of the counter-rotating field (`2 omega` on the axis, `-2 * s_o * omega` in a solid-body outer layer),
- zero-centred ranges, the diverging palette and the temporal averager,
- profile pane switching rules and presets,
- lookup accuracy at the 512-sample default and equality of one boundary query with two separate calls,
- a 2400-step counter-rotating run: both senses present, absolute angular velocity above zero, radial distribution near its
  no-swirl level, all values finite.

The ScalaFX/JavaFX renderers and panes cannot be exercised by the headless unit tests; the logic they use is kept in
plain classes (`VortexProfileEdits`, `DivergingPalette`, `RotationSenseClassifier`, `TemporalFieldAverager`) so it is tested.

## Scientific scope

Particles are Lagrangian markers representing elements of a modeled fluid flow; they are not literal water molecules. Units are simulation units. The swirl and secondary-flow models are designed for visualization and controlled comparison, not as a substitute for validated CFD.

## Roadmap

The natural next milestone is 0.8: geometry-induced swirl, where the tangent direction follows the twist of the pipe so that the swirl is driven by the geometry rather than imposed, together with side-by-side comparison of two profiles in the same geometry.

## Documentation

- `README.md` — project and milestone overview
- `docs/ARCHITECTURE.md` — architecture and dependency boundaries
- `docs/BENCHMARKS.md` — 0.6 versus 0.7 measurements and how to reproduce them
- `CHANGELOG.md` — milestone history

## License

Licensed under the **Apache License, Version 2.0**. See `LICENSE`.
