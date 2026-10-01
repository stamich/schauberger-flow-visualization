# Milestone 0.3 implementation tasks

## 1. Establish the 0.3 baseline
- Start from 0.2.0.
- Bump Gradle project version to 0.3.0.
- Keep Scala 2.13.15 / ScalaFX / ScalaTest baseline.
- Acceptance: old architectural layers still exist and UI is not referenced from physics.

## 2. Retune startup defaults
- Change particle count 1500 → 750.
- Change axial velocity 120 → 90.
- Change angular velocity 0.90 → 0.55.
- Change swirl response 3.0 → 2.0.
- Move trail settings out of `SimulationParameters`.
- Acceptance: `DefaultParametersSpec` verifies values.

## 3. Separate parameter domains
- Add `GeometryParameters`.
- Add `VisualizationParameters`.
- Keep physical values in `SimulationParameters`.
- Acceptance: trail/view settings are absent from physical parameters.

## 4. Introduce generic `CrossSectionShape`
- Define contains, signed distance, inward normal, clamp and parametric boundary API.
- Acceptance: physics can query boundaries without a radius property.

## 5. Implement `CircularCrossSection`
- Preserve circular baseline behavior.
- Add unit tests for containment, distance and clamp.

## 6. Implement `OvoidCrossSection`
- Use elliptical polar radius plus bounded asymmetry term.
- Implement signed radial gap, numeric inward normal, clamp and boundary sampling.
- Acceptance: opposite vertical radii differ for non-zero asymmetry.

## 7. Generalize `PipeGeometry`
- Add local cross-section transforms.
- Add local frame, signed distance, inward normal and clamp helpers.
- Add characteristic/bounding radius and twist metadata.

## 8. Implement `StraightPipe`
- Support arbitrary cross-section without twist.
- Keep `StraightCircularPipe` as convenience compatibility geometry.

## 9. Implement `TwistedPipe`
- Implement theta(x)=2*pi*N*x/L.
- Rotate world coordinates to/from local cross-section space.
- Produce local normal/binormal frame.
- Acceptance: transform round-trip error is near zero.

## 10. Add `GeometryFactory`
- Map `Circular`, `Ovoid`, `TwistedOvoid` settings to domain geometry.
- Acceptance: UI/controller need not construct shape classes directly.

## 11. Generalize axial flow
- Compute current/target axial speed along local tangent.
- Acceptance: force remains valid for future curved centerlines.

## 12. Generalize swirl
- Retain target-velocity `SwirlProfile` model.
- Use geometry characteristic radius/local tangent.
- Acceptance: CW and CCW accelerations oppose one another.

## 13. Generalize wall repulsion
- Remove circular `R-r` calculation.
- Use signed boundary distance and inward normal.
- Acceptance: same class works with ovoid and twisted ovoid.

## 14. Generalize hard boundaries
- Replace radial clamp with `PipeGeometry.clampInside`.
- Remove outward velocity component after correction.
- Acceptance: long runs preserve `geometry.contains` invariant.

## 15. Generalize particle generation
- Replace circle-specific sampling with deterministic rejection sampling in local cross-section space.
- Acceptance: all generated particles fit every geometry and seeds are reproducible.

## 16. Make `SimulationEngine` geometry-agnostic
- Pass geometry to `initialState` and `step` rather than constructor.
- Acceptance: same engine instance can run all geometry benchmarks.

## 17. Add geometry-aware metrics
- Preserve axial/tangential/angular metrics.
- Add mean normalized radial position and twist rate display.

## 18. Rework cross-section view
- Add selectable axial slice position.
- Render only particles in a finite slice band.
- Draw the actual rotated parametric boundary.

## 19. Add twist visualization markers
- Render sparse longitudinal orientation hints when twist is non-zero.
- Keep this visualization-only.

## 20. Implement fading trails
- Add timestamped `TrailSample`.
- Add age-based and length-based eviction.
- Sample every N physics frames.
- Fade segment alpha linearly with age.
- Acceptance: trails disappear after default 2 seconds and do not grow forever.

## 21. Expand controls
- Add geometry selector.
- Add twist-turn slider.
- Add cross-section position slider.
- Add trail fade-time slider.
- Retune ranges for 750-particle defaults.

## 22. Update controller
- Store physical, geometry and visualization parameters independently.
- Rebuild/reset only when geometry changes.
- Clear trails on reset/geometry change.

## 23. Expand benchmark suite
- Add circular axial, circular swirl, ovoid swirl and twisted-ovoid swirl scenarios.
- Run 1k/10k/50k particle sizes.
- Write `benchmark-0.3.0.json`.

## 24. Update automated tests
- Add shape contract tests, twist transform tests, generator tests, geometry-aware force tests, long-run invariants and trail expiry tests.

## 25. Update documentation and release notes
- Update README, ARCHITECTURE and CHANGELOG.
- Describe the model as Schauberger-inspired, not as validated Schauberger physics.

## Definition of Done
- All three geometry modes use the same simulation/force/boundary classes.
- Default population is 750.
- Trails visibly fade and are age bounded.
- Cross-section view is a real axial slice.
- Benchmark JSON supports geometry comparisons.
- `gradle clean test`, `gradle run`, and `gradle benchmark` succeed in a Gradle-enabled environment.
