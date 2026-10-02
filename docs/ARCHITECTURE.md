# Architecture — milestone 0.6

## Purpose

Milestone 0.6 keeps the 0.5 separation between physics, diagnostics and ScalaFX while optimizing the ovoid hot path and making the tangential vortex model pluggable.

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

## Physics pipeline

```text
Particle
  ↓
GeometryContextCalculator
  ↓
ParticleGeometryContext
  ↓
ParticleFlowContext
  ├── AxialFlowForce
  ├── SwirlForce
  ├── SecondaryFlowForce
  └── WallRepulsionForce
  ↓
CompositeFlowForce
  ↓
SemiImplicitEulerIntegrator
  ↓
PipeBoundaryHandler
```

Local frame, local position, boundary distance, inward normal, twist angle and twist rate are computed once per particle step and reused by all forces.

## Ovoid kernel

```text
OvoidCrossSection
       ↓
OvoidGeometryKernel
   ┌───────┴────────┐
   ↓                ↓
Exact            Lookup
analytic      OvoidBoundaryLookup
```

`ExactOvoidGeometryKernel` is the correctness reference. `LookupOvoidGeometryKernel` is the default runtime implementation. `OvoidBoundaryLookup` precomputes angle, radius, radius derivative and inward normal and performs periodic linear interpolation.

## Vortex profiles

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

Only `SwirlProfileFactory` maps profile ADTs to concrete strategies. The factory caches strategies by immutable parameter value to avoid per-particle allocations.

## Diagnostics

The 0.5 diagnostics pipeline is preserved:

```text
SimulationState
  ↓
CrossSectionSpatialIndex
  ↓
CrossSectionVelocityFieldSampler
  ├── ScalarFieldCalculator
  └── VorticityFieldCalculator
  ↓
HeatMapRenderer
```

0.6 adds `RadialProfileSampler`, which samples an analytic vortex profile independently of particles and reports peak tangential velocity and normalized peak radius.

## UI

```text
ControlPanel
├── FlowControlPane
├── GeometryControlPane
├── VisualizationControlPane
└── DiagnosticsControlPane
```

`FlowControlPane` adds a profile selector and profile-specific numeric controls. `GeometryControlPane` adds Exact/Lookup selection and lookup table resolution. Every slider still uses `NumericSliderField`, so each value can also be typed manually.

## Benchmarks

The application benchmark measures complete simulation scenarios after warm-up and repeated measurements. The geometry section compares exact and lookup context throughput. The accuracy section compares lookup resolutions 256/512/1024/2048 against exact equations.

JMH is reserved for microbenchmarks of exact-vs-lookup geometry kernels and the three vortex profile functions.
