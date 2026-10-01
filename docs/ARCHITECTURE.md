# Architecture — milestone 0.5

## Purpose

Milestone 0.5 adds flow diagnostics while reducing repeated geometry work discovered by the 0.4 benchmark.

## Dependency direction

```text
ScalaFX UI
  ↓
SimulationController
  ↓
SimulationEngine
  ↓
GeometryContextCalculator
  ↓
ParticleGeometryContext
  ↓
ParticleFlowContext
  ↓
CompositeFlowForce
  ├── AxialFlowForce
  ├── SwirlForce
  ├── SecondaryFlowForce
  └── WallRepulsionForce
```

Physics and diagnostics do not import ScalaFX.

## Shared geometry context

For each particle, `SimulationEngine` asks `GeometryContextCalculator` for exactly one context before force calculation. The context contains center-line location, local cross-section position, local frame, signed wall distance, inward normal and twist metadata.

This prevents each force from independently repeating `toLocalCrossSection`, `localFrameAt`, `signedDistance` and normal calculations.

## Ovoid geometry optimization

`OvoidGeometryMath` computes:

- polar radius,
- radius derivative,
- boundary point,
- analytic inward normal.

The previous finite-difference normal required multiple signed-distance calls. The analytic normal requires only one polar-angle evaluation plus closed-form derivative calculations.

## Local frame

`LocalFrame` owns:

- origin,
- tangent,
- normal,
- binormal,
- `Rotation2D`.

`Rotation2D` caches angle, sine and cosine, and provides forward/inverse 2D transforms.

## Diagnostics pipeline

```text
SimulationState
  ↓
CrossSectionVelocityFieldSampler
  ↓
VectorFieldGrid
  ├── ScalarFieldCalculator
  │     ├── velocity magnitude
  │     ├── axial velocity
  │     ├── tangential velocity
  │     └── secondary velocity
  └── VorticityFieldCalculator
        ↓
ScalarFieldGrid
  ↓
HeatMapRenderer
```

The sampler interpolates particle velocity around the selected physical axial slice. Diagnostics are render/read-only and never feed back into the physics engine.

## UI decomposition

```text
ControlPanel
├── FlowControlPane
├── GeometryControlPane
├── VisualizationControlPane
└── DiagnosticsControlPane
```

Every numeric slider is implemented through `NumericSliderField`, which pairs ScalaFX `Slider` and `TextField` controls with pure validation in `NumericValueCodec`.

## Benchmark architecture

The 0.5 benchmark deliberately runs outside JavaFX. It contains three groups:

1. particle simulation scenarios,
2. secondary vector-field sampling,
3. geometry-context microbenchmarks.

Warm-up and repeated measurements reduce the JIT bias visible in the 0.4 results. Median and p95 values are persisted to JSON.

## CI

Normal CI executes compilation and tests. Benchmarks are isolated in a manual workflow so performance runs do not make every pull request slow or noisy.

## Extension points

The 0.5 architecture prepares the project for:

- Rankine and Lamb–Oseen swirl profiles,
- additional vorticity/pressure diagnostics,
- spatial indexing for larger particle populations,
- JMH benchmarks,
- comparison mode between multiple flow profiles.
