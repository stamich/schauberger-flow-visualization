# Architecture — milestone 0.4

## Purpose

Milestone 0.4 extends the geometry-first architecture with a secondary cross-sectional flow model. Geometry now influences motion in two distinct ways:

1. as a hard/soft boundary,
2. as input to a local secondary-flow target field.

The model remains intentionally lighter than CFD.

## Dependency direction

```text
ScalaFX
  ↓
UI panes + NumericSliderField
  ↓
SimulationController
  ↓
SimulationEngine
  ↓
CompositeFlowForce
  ├── AxialFlowForce
  ├── SwirlForce
  ├── SecondaryFlowForce
  └── WallRepulsionForce
        ↓
PipeGeometry + immutable model + math
```

Physics never imports ScalaFX.

## Parameter composition

```text
SimulationParameters
├── particleCount
├── AxialFlowParameters
├── SwirlParameters
├── SecondaryFlowParameters
├── WallParameters
├── maxVelocity
└── fixedTimeStep
```

`GeometryParameters` and `VisualizationParameters` remain separate because geometry reconstruction and rendering configuration have different lifecycle rules from physical parameters.

## Secondary-flow model

`SecondaryFlowModel` returns a target velocity in local cross-section coordinates:

```text
world particle
    ↓ PipeGeometry.toLocalCrossSection
local (u,v)
    ↓ SecondaryFlowModel
target local velocity
    ↓ LocalFrame.crossSectionVectorToWorld
world target velocity
    ↓ SecondaryFlowForce
acceleration contribution
```

`SecondaryFlowForce` is responsible for first-order response toward that target. The model itself does not know about particle acceleration integration.

## Current profile

`TwinVortexSecondaryFlow` is a smooth stream-function-inspired educational field. It depends on:

- local cross-section position,
- local twist rate,
- secondary strength,
- boundary attenuation.

`NoSecondaryFlow` is available for regression and explicit neutral behavior.

## Boundary attenuation

```text
BoundaryAttenuation
└── LinearBoundaryAttenuation
```

The model approaches zero near the wall using signed distance supplied by `CrossSectionShape`. This keeps the profile independent of circular/ovoid shape details.

## Local frame

`LocalFrame` contains:

- tangent,
- normal,
- binormal,
- cross-section rotation angle.

It also converts vectors between local cross-section space and world space. `TwistedPipe` therefore rotates both geometry and modeled secondary velocity consistently.

## Field sampling and rendering

```text
SecondaryFlowModel
    ↓
SecondaryFlowFieldSampler
    ↓ Vector[FieldSample]
VectorFieldRenderer
```

The sampler is domain-side and ScalaFX-free. `VectorFieldRenderer` is presentation-only.

This separation allows the sampling cost to be benchmarked without starting JavaFX.

## Velocity diagnostics

`VelocityDecomposer` projects particle velocity into the local frame. `FlowMetricsCalculator` reports:

- mean axial velocity,
- mean tangential velocity,
- mean angular velocity,
- vorticity proxy,
- normalized radial position,
- mean modeled secondary velocity,
- secondary-flow energy-ratio proxy.

The last two values describe the simplified model and should not be interpreted as CFD-derived physical measurements.

## UI decomposition

The former monolithic control panel is split into:

```text
ControlPanel
├── FlowControlPane
├── GeometryControlPane
└── VisualizationControlPane
```

This reduces inheritance-name collisions with ScalaFX controls and makes each section easier to test and extend.

Every numeric control uses:

```text
NumericSliderField
├── Slider
├── TextField
└── NumericValueCodec
```

`NumericValueCodec` is pure logic. This allows parsing/clamping behavior to be unit tested without starting the JavaFX toolkit.

## State and mutability

Physical state remains immutable (`SimulationState`, `Particle`). Runtime/UI state lives in `SimulationController`. `TrailBuffer` remains intentionally mutable but visualization-only.

## Extension points for 0.5+

- Eulerian velocity-grid cache,
- velocity magnitude heat map,
- vorticity heat map,
- additional `SecondaryFlowModel` profiles,
- additional `SwirlProfile` implementations,
- quantitative field comparisons.
