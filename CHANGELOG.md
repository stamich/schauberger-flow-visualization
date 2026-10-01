# Changelog

All notable changes to this project are documented here.

## [0.5.0] - 2026-10-01

### Added
- `ParticleGeometryContext`, `GeometryContextCalculator` and `ParticleFlowContext` for shared per-particle geometry data.
- `Rotation2D` with cached sine/cosine values.
- `OvoidGeometryMath` with analytic polar-radius derivatives and inward normals.
- Diagnostics package with vector/scalar grids, field types, range normalization and velocity interpolation.
- Cross-sectional vorticity estimate and solid-body `2*omega` validation test.
- `HeatMapRenderer` with selectable velocity/vorticity fields and legend.
- `DiagnosticsControlPane`, heat-map controls and independent particle/trail/vector layer toggles.
- Benchmark warm-up, repeated measurements, median and p95 statistics.
- Geometry-context microbenchmark for circular, ovoid and twisted-ovoid geometries.
- GitHub Actions `ci.yml` for push/PR/manual test builds.
- Manual `benchmark.yml` workflow with benchmark JSON artifact upload.
- Apache License 2.0 in the repository root.

### Changed
- `FlowForce` now consumes `ParticleFlowContext`.
- `SimulationEngine` computes local geometry once per particle before force evaluation.
- Axial, swirl, secondary and wall forces reuse cached geometry values.
- `VelocityDecomposer` and `FlowMetricsCalculator` reuse precomputed local geometry.
- `OvoidCrossSection.inwardNormal` no longer uses finite-difference signed-distance sampling.
- `LocalFrame` now includes origin and `Rotation2D`.
- Cross-section rendering can show heat maps beneath vector/particle overlays.
- Benchmark output moved to `benchmark/results/benchmark-0.5.0.json`.

### Preserved
- 750-particle readable startup default.
- Editable numeric input next to every slider.
- Two-second fading particle trails.
- Fixed-step semi-implicit Euler integration.
- Circular, ovoid and twisted-ovoid geometry.
- Geometry-induced secondary flow.
- Domain/UI separation and ScalaFX Canvas rendering.

## [0.4.0-buildfix1] - 2026-10-01

### Fixed

- Fixed `NumericSliderField` compilation with ScalaFX by constructing `TextField` with the no-argument ScalaFX constructor and assigning its `text` property explicitly.
- Preserved bidirectional slider/text synchronization and numeric validation behavior.

## [0.4.0] - 2026-10-01

### Added
- `SecondaryFlowModel` abstraction for geometry-induced cross-sectional target velocity fields.
- `NoSecondaryFlow` neutral model.
- `TwinVortexSecondaryFlow` smooth two-cell educational field driven by local twist.
- `BoundaryAttenuation` and `LinearBoundaryAttenuation` for wall fading.
- `SecondaryFlowForce` integrated into the existing composite force pipeline.
- `VelocityComponents` and `VelocityDecomposer`.
- `FieldSample` and `SecondaryFlowFieldSampler`.
- `VectorFieldRenderer` for cross-section secondary-flow arrows.
- Mean secondary-velocity and secondary-flow energy-ratio diagnostics.
- `AxialFlowParameters`, `SwirlParameters`, `SecondaryFlowParameters` and `WallParameters`.
- `NumericSliderField`, pairing every numeric slider with editable numeric input.
- `NumericValueCodec` for pure parsing, validation, clamping, rounding and formatting.
- `FlowControlPane`, `GeometryControlPane` and `VisualizationControlPane`.
- UI controls for enabling secondary flow, secondary strength/response, wall fade, vector-field visibility and grid resolution.
- Secondary-flow tests covering zero-twist behavior, symmetry, finite values and wall attenuation.
- Numeric input tests for dot/comma decimals, invalid values, range clamping and integer rounding.
- `twisted-ovoid-swirl-secondary` benchmark scenario.
- 15x15 / 30x30 / 60x60 vector-field sampling benchmarks.
- `benchmark/results/benchmark-0.4.0.json` output.

### Changed
- `SimulationParameters` is now composed from focused parameter records instead of one flat numeric record.
- `LocalFrame` now converts vectors between local cross-section coordinates and world space.
- `PipeGeometry` exposes `twistRateAt(x)` as an extension point for future non-uniform twist.
- `FlowMetricsCalculator` receives the active simulation parameters and secondary-flow model.
- `SimulationRenderer` can render the secondary vector field using the active physical parameters.
- The monolithic 0.3 `ControlPanel` is now only a composition container for focused panes, reducing ScalaFX inherited-property collision risk.
- Status display includes modeled secondary velocity and secondary-energy ratio.
- Benchmark suite now measures both particle simulation and field sampling.

### Preserved
- 750-particle readable startup default.
- Axial velocity 90.0, angular velocity 0.55 and swirl response 2.0 defaults.
- 48-sample, 2-second fading trails sampled every four physics frames.
- Immutable physical simulation state.
- Geometry-independent `SimulationEngine`.
- Semi-implicit Euler integration and fixed timestep.
- Circular, ovoid and twisted-ovoid geometry modes.
- ScalaFX Canvas rendering and separation between domain physics and UI.

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
