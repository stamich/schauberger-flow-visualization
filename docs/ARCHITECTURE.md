# Architecture — milestone 0.3

## Purpose

Milestone 0.3 makes geometry a first-class domain abstraction. Physics operates on signed boundary distance, inward normals, local frames and coordinate transforms rather than assuming a circular radius.

## Main abstractions

```text
CrossSectionShape
├── CircularCrossSection
└── OvoidCrossSection

PipeGeometry
├── StraightPipe
├── StraightCircularPipe   (convenience baseline)
└── TwistedPipe
```

`GeometryFactory` is the only component translating UI-level `GeometryParameters` into concrete geometry instances.

## Local coordinates

For a twisted pipe, world `(y,z)` coordinates are rotated by `-theta(x)` before shape tests. Local coordinates are rotated back by `+theta(x)` for rendering/generation.

```text
world particle
    ↓ toLocalCrossSection
local (u,v)
    ↓ CrossSectionShape
contains / distance / normal / clamp
```

## Physics dependency

`AxialFlowForce`, `SwirlForce`, and `WallRepulsionForce` depend only on `PipeGeometry`.

- Axial flow uses `tangentAt`.
- Swirl uses centerline + local tangent.
- Wall repulsion uses `signedDistanceToBoundary` + `inwardNormal`.
- `PipeBoundaryHandler` uses `clampInside`.

There is no shape-specific branching in physics.

## Runtime geometry changes

`SimulationEngine` no longer owns one geometry. Geometry is passed to `initialState` and `step`, so `SimulationController` can replace the active geometry and reset particles without rebuilding the engine.

## Parameter separation

```text
SimulationParameters
  physical/numerical values

GeometryParameters
  geometry type and dimensions

VisualizationParameters
  view, trails and cross-section slice
```

This prevents UI-only properties such as trail lifetime from contaminating physical simulation state.

## Trails

`TrailBuffer` is intentionally mutable but render-only. Each `TrailSample` contains position and simulation time. Samples are recorded every N physics frames and removed when they exceed either maximum length or maximum age. The immutable simulation state is unaffected.

## Cross-section rendering

The selected slice is:

```text
sliceX = pipe.length * crossSectionFraction
```

Only particles within `crossSectionSliceHalfWidth` are shown. The boundary is sampled parametrically from the actual rotated cross-section.

## Future extension point

Milestone 0.4 can add `SecondaryFlowForce` derived from twist/curvature while preserving the engine and geometry contracts.
