# Changelog

All notable project changes are documented in this file.

## [0.2.0] - 2026-10-01

### Added

- Three-dimensional `Vector3D` simulation mathematics including dot and cross products.
- Three-dimensional particle position and velocity.
- `RotationDirection` ADT for clockwise and counter-clockwise swirl.
- `ViewMode` ADT for longitudinal and cross-section visualization.
- `SwirlProfile` abstraction.
- `SolidBodySwirlProfile` with `v_theta = omega * r`.
- `SwirlForce` using target tangential velocity rather than unbounded angular acceleration.
- Three-dimensional circular-pipe center-line, tangent and radial-distance operations.
- Circular radial wall repulsion and hard radial boundary correction.
- Uniform-area circular cross-section particle generation.
- Longitudinal and cross-section `Projection` implementations.
- Render-only bounded `TrailBuffer` and `TrailRenderer`.
- Cross-section pipe visualization.
- Live angular-velocity, swirl-response and trail-length controls.
- Live clockwise/counter-clockwise rotation selection.
- Live projection selection.
- `FlowMetrics` and `FlowMetricsCalculator` for axial, tangential and angular diagnostics.
- Solid-body vorticity proxy.
- Axial-only versus axial-plus-swirl benchmark scenarios.
- JSON benchmark output in `benchmark/results/benchmark-0.2.0.json`.
- New and updated unit/integration tests for 3D motion, swirl and projections.

### Changed

- Migrated simulation domain from 2D to 3D while retaining `Vector2D` for screen projections.
- Generalized `PipeGeometry`, force strategies, integration, generation and boundaries to 3D.
- Generalized `ViewportTransform` to arbitrary projected bounds.
- Updated Canvas rendering to work through projection strategies.
- Updated application wiring and UI for milestone 0.2.
- Updated README and architecture documentation.

### Preserved

- Immutable physical `SimulationState`.
- Fixed-timestep physics.
- Semi-implicit Euler integration.
- Dependency direction from UI toward domain, never the reverse.
- Constant particle population through outlet-to-inlet respawn.

## [0.1.0] - 2026-10-01

### Added

- Initial Scala/ScalaFX application.
- Immutable particle simulation state.
- Two-dimensional vector mathematics.
- Straight circular-pipe longitudinal geometry.
- Axial target-velocity flow model.
- Soft wall repulsion.
- Semi-implicit Euler integration.
- Continuous particle respawn.
- Fixed-timestep simulation controller.
- Canvas rendering and controls.
- Initial tests, benchmark and documentation.
