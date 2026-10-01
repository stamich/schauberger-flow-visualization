# Changelog

All notable changes to this project are documented here.

## [0.3.0-buildfix1] - 2026-10-01

### Fixed
- Fixed `ControlPanel` compilation failure caused by slider fields named `width` and `height` shadowing inherited ScalaFX `VBox.width` / `VBox.height` properties of type `ReadOnlyDoubleProperty`.
- Renamed the controls to `widthSlider` and `heightSlider` and updated all references.

## [0.3.0] - 2026-10-01

### Added
- `CrossSectionShape` abstraction.
- `CircularCrossSection` and asymmetric `OvoidCrossSection`.
- `StraightPipe` for arbitrary untwisted cross-sections.
- `TwistedPipe` with `theta(x) = 2*pi*N*x/L`.
- `LocalFrame` and local/world cross-section transforms.
- `GeometryType`, `GeometryParameters`, `GeometryFactory`.
- `VisualizationParameters` separated from physical settings.
- Geometry selector for Circular / Ovoid / Twisted ovoid.
- Adjustable twist turns.
- Adjustable physical cross-section slice position.
- Geometry-aware boundary rendering.
- Mean normalized radial-position metric and twist-rate status.
- Timestamped `TrailSample` history.
- Age-based trail expiration.
- Linear trail opacity fade.
- Reduced trail sampling cadence.
- Geometry benchmark scenarios and `benchmark-0.3.0.json` output.
- Tests for ovoid geometry, transforms, factory, trail expiry, arbitrary-shape generation and long-running geometry invariants.

### Changed
- Default particle count reduced from 1500 to **750**.
- Default axial velocity reduced from 120.0 to **90.0**.
- Default angular velocity reduced from 0.90 to **0.55**.
- Default swirl response reduced from 3.0 to **2.0**.
- Default trail length changed to **48 samples**.
- Default trail lifetime set to **2.0 seconds**.
- Trails are sampled every **4 physics frames** instead of every frame.
- `PipeGeometry` generalized beyond a circular radius.
- `WallRepulsionForce` now uses signed boundary distance and inward normal.
- `PipeBoundaryHandler` now clamps through the geometry abstraction.
- `UniformInletParticleGenerator` replaced by geometry-agnostic `UniformCrossSectionParticleGenerator`.
- `SimulationEngine` no longer owns a fixed geometry.
- Cross-section rendering now displays a finite physical axial slice rather than collapsing the entire pipe.

### Preserved
- Immutable physical simulation state.
- Semi-implicit Euler integration.
- Target axial-velocity controller.
- Solid-body swirl profile and target tangential velocity response.
- Fixed timestep simulation.
- Canvas-based ScalaFX rendering.
- Clear separation between domain simulation and UI.

## [0.2.0] - 2026-10-01

### Added
- 3D particle state and `Vector3D` mathematics.
- Solid-body swirl profile and configurable `SwirlForce`.
- Clockwise/counter-clockwise rotation.
- Longitudinal and cross-section projections.
- Particle trail rendering.
- Flow metrics and axial-vs-swirl benchmark JSON output.

## [0.1.0] - 2026-10-01

### Added
- Initial ScalaFX particle-flow visualization.
- Immutable simulation state and 2D vector foundation.
- Straight circular pipe, axial flow, wall repulsion and boundary handling.
- Semi-implicit Euler integration, controls, tests and documentation.
