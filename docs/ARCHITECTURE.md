# Architecture — milestone 0.7

## Purpose

Milestone 0.7 keeps the separation between physics, diagnostics and ScalaFX, adds a counter-rotating vortex, and corrects
the force model so that swirl behaves like a vortex: tracers stay on their orbit and secondary flow no longer cancels
swirl.

## Package layout

```text
io.codeswarm.schauberger
├── application
├── benchmark
├── diagnostics
├── geometry
│   └── ovoid
├── math
├── model
├── physics
│   ├── secondary
│   └── swirl
├── simulation
├── ui
└── visualization
```

Everything outside `ui`, `visualization` (the renderers), `application` and the JavaFX Gradle block compiles and is tested
without JavaFX. Logic that a ScalaFX class needs is kept in plain classes so it can be unit tested:
`VortexProfileEdits`, `DivergingPalette`, `RotationSenseClassifier`, `RotationSensePalette`, `TemporalFieldAverager`.

## Physics pipeline

```text
Particle
  ↓
GeometryContextCalculator          one CrossSectionShape.boundaryQuery per particle
  ↓
ParticleGeometryContext
  ↓
ParticleFlowContext
  ├── AxialFlowForce
  ├── CrossSectionFlowForce        swirl profile + secondary-flow target, once per axis
  ├── CentripetalBalanceForce      -v_theta^2 / r toward the axis
  └── WallRepulsionForce
  ↓
CompositeFlowForce                 assembled only by DefaultFlowForces
  ↓
SemiImplicitEulerIntegrator
  ↓
PipeBoundaryHandler
```

Local frame, local position, boundary distance, inward normal, twist angle and twist rate are computed once per particle
step and reused by all forces.

### CrossSectionFlowForce

Swirl and the twin-vortex secondary field both act in the cross-section. Relaxed independently, they pulled the same
tangential component toward different targets and swirl settled at `k_s / (k_s + k_2)` of its profile value. The force
resolves the in-plane velocity on the local radial and tangential axes:

- tangential: `a_t = k_s * (v_profile * sign + S_t - v_t)`, where `S_t` is the tangential part of the secondary target
  (the secondary response is used when swirl is off),
- radial: `a_r = k_2 * (S_r - v_r)`, only while secondary flow is enabled.

With secondary flow off it is the swirl relaxation of earlier milestones. The secondary target does not depend on the
rotation direction; the profile target is multiplied by the sign.

### CentripetalBalanceForce

`a = -strength * v_theta^2 / max(r, 0.05 * R_char)` along the inward radial direction, from the tracer's current
tangential speed. It is independent of the rotation direction (the speed is squared), has no axial component and is zero
on the axis. `RadialBalanceParameters(enabled, strength)` controls it; the default is enabled with strength 1.0.

## Vortex profiles

```text
SwirlParameters.profile
       ↓
VortexProfileParameters
   ├── SimpleVortexProfileParameters:  SolidBody · Rankine · LambOseen
   └── CounterRotatingParameters(inner, outer, s_i, s_o, r_t, delta)
       ↓
SwirlProfileFactory
       ↓
SwirlProfile  (signed tangential speed)
   ├── SolidBodySwirlProfile · RankineVortexProfile · LambOseenVortexProfile
   └── CounterRotatingVortexProfile(inner, outer, s_i, s_o, TransitionFunction)
                                                            └── TanhTransition
```

`CounterRotatingParameters` accepts only `SimpleVortexProfileParameters` layers, so composites never nest and the factory
needs no recursion. `SwirlProfile.tangentialVelocity` is a signed contract: positive turns with the configured rotation
direction, negative against it. Consumers must not assume `speed >= 0`.

`SwirlProfileFactory` remembers only the most recently requested parameters (compared by reference first, then by value).
All particles of a step share one parameter object, so the hot path is a reference comparison, and a slider drag replaces
the entry instead of growing a map.

## Ovoid kernel

```text
OvoidCrossSection
       ↓  boundaryQuery(point) = BoundaryQuery(signedDistance, inwardNormal)
OvoidGeometryKernel   rayAt(angle) = BoundaryRay(radius, inwardNormal)
   ┌───────┴────────┐
   ↓                ↓
Exact            Lookup
analytic      OvoidBoundaryLookup (primitive arrays, 512 samples by default)
```

`ExactOvoidGeometryKernel` is the correctness reference. `LookupOvoidGeometryKernel` is the default runtime
implementation. The geometry context used to call `signedDistance` and `inwardNormal` separately, each repeating `atan2`
and the table lookup; `boundaryQuery` shares them. Other shapes inherit a default that simply calls both.

## Diagnostics

```text
SimulationState ──► CrossSectionSpatialIndex ──► CrossSectionVelocityFieldSampler ─┐
                                                                                   ├─► VectorFieldGrid
SwirlParameters ──► AnalyticProfileFieldSampler ───────────────────────────────────┘
                                   ↓
ScalarFieldCalculator ◄── VorticityFieldCalculator
                                   ↓
TemporalFieldAverager  (restarts when field, source or resolution changes)
                                   ↓
HeatMapRenderer  (min-max ramp, or DivergingPalette on a zero-centred range for signed fields)
```

Aggregate metrics (`FlowMetricsCalculator`) now include:

- `occupancy`: share of tracers in the inner, middle and outer third of the normalized radius (about 11 / 33 / 56 % for an
  area-uniform population),
- `regions`: `VortexRegionMetrics` with forward, reverse and quiescent shares and mean angular velocities. A tracer is
  quiescent below 5% of the configured angular velocity. The signed mean angular velocity is kept, but it cancels for a
  counter-rotating flow, so the absolute mean is reported next to it.

`RadialProfileSampler` samples an analytic profile independently of particles. Its peak is the sample of largest
magnitude, `ZeroCrossingDetector` places sign changes by linear interpolation, and `VortexProfileDiagnostics` carries the
crossings and the most negative velocity.

## UI

```text
ControlPanel
├── FlowControlPane            particles, axial/swirl, secondary flow, centripetal balance, direction
├── VortexProfileControlPane   profile kind, profile sliders, counter-rotating sliders and presets
├── GeometryControlPane
├── VisualizationControlPane   view, trails, layers, colour by rotation sense
└── DiagnosticsControlPane     heat-map field, source, grid, smoothing
```

`VortexProfileControlPane` shows only the fields of the selected kind and moves the sliders to the stored values after a
preset or a kind switch. The rules (which values survive a switch, which slider edits which profile) live in
`VortexProfileEdits`. Every slider still uses `NumericSliderField`, so each value can also be typed.

`RotationSenseClassifier` is shared by the particle and trail renderers and applies the same rule as the aggregate
region metrics, so the colours and the status-bar shares cannot disagree.

## Benchmarks

The application benchmark measures complete simulation scenarios after warm-up and repeated measurements, field sampling,
geometry-context throughput and lookup accuracy; it writes `benchmark/results/benchmark-0.7.0.json`. JMH covers
microbenchmarks of the kernels, the single-ray lookup, the cross-section query and the profile functions. See
`docs/BENCHMARKS.md`.

## Deliberately not in 0.7

Geometry-induced swirl — the tangent direction following the twist of the pipe so that swirl is produced by the geometry
rather than imposed by a profile — changes what a profile means and is planned for 0.8.
