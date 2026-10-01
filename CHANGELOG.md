# Changelog

All notable changes to this project are documented in this file.

## [0.1.0] - 2026-10-01

### Added

- Initial ScalaFX application and project structure.
- Immutable particle, simulation state, and simulation parameter models.
- `Vector2D` mathematics with safe normalization and speed limiting.
- Straight 2D representation of a circular pipe.
- Axial target-velocity flow force.
- Soft wall-repulsion force.
- Composite force model.
- Semi-implicit Euler particle integrator.
- Uniform inlet particle generator with deterministic seed support.
- Pipe boundary handling with outlet-to-inlet respawn.
- Fixed-timestep simulation engine.
- Canvas-based pipe and particle rendering.
- World-to-screen viewport transformation.
- Start, pause, reset, particle-count, and axial-velocity controls.
- FPS display.
- Unit and integration tests for math, geometry, physics, simulation, and viewport mapping.
- Lightweight headless simulation benchmark.
- Architecture and detailed implementation-task documentation.

### Notes

Milestone 0.1 intentionally implements only axial flow. Vortex/swirl behaviour is reserved for milestone 0.2 so that the baseline flow and architecture can be verified independently.
