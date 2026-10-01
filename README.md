# Schauberger Flow Visualization

> Buildfix: **0.4.0-buildfix1** fixes ScalaFX `TextField` construction in `NumericSliderField`.

Scala/ScalaFX particle visualization of helical and geometry-driven flow concepts inspired by Viktor Schauberger's observations of water vortices.

> This project is an educational and experimental visualization. It does **not** claim to validate Schauberger's broader physical theories and it is not a Navier-Stokes CFD solver.

## Milestone 0.4.0

Milestone 0.4 adds **geometry-induced secondary flow** to the 0.3 geometry model. A twisted ovoid pipe can now influence the local cross-sectional target velocity field in addition to axial flow and swirl.

The physical acceleration model is conceptually:

\[
\vec a = \vec a_{axial} + \vec a_{swirl} + \vec a_{secondary} + \vec a_{wall}
\]

The new secondary model is intentionally named `TwinVortexSecondaryFlow`: it is a smooth educational vector field shaped by local twist and wall distance, not a claim that the simulation reproduces a specific measured Schauberger device or a complete Dean-vortex solution.

## Scala / ScalaFX baseline

- Scala 2.13.15
- ScalaFX 21
- JavaFX 21
- JDK 21
- Gradle
- ScalaTest + JUnitRunner

The domain simulation remains independent of ScalaFX. ScalaFX is used only for application composition, controls and Canvas rendering.

## Default parameters

Defaults remain deliberately readable rather than dense:

| Parameter | Default |
|---|---:|
| Particles | **750** |
| Axial velocity | **90.0** |
| Axial response | **2.2** |
| Angular velocity | **0.55** |
| Swirl response | **2.0** |
| Secondary flow | **enabled** |
| Secondary strength | **18.0** |
| Secondary response | **1.5** |
| Boundary fade distance | **18.0** |
| Trail samples | **48** |
| Trail lifetime | **2.0 s** |
| Trail sample interval | **4 physics frames** |
| Vector-field grid | **15 x 15** |

All values are simulation units unless explicitly stated otherwise.

## Editable sliders

Every numeric slider in 0.4 has a synchronized editable numeric field.

- moving the slider updates the text field,
- pressing Enter in the text field updates the slider,
- leaving the text field commits the value,
- `.` and `,` decimal separators are accepted,
- out-of-range input is clamped to the slider range,
- integer controls such as particle count and vector-grid resolution are rounded,
- invalid/non-finite values are rejected and the current valid value is restored.

The parsing/clamping logic lives in the pure `NumericValueCodec`, while `NumericSliderField` is the ScalaFX adapter.

## Flow parameter model

`SimulationParameters` is now composed from smaller immutable groups:

```text
SimulationParameters
├── AxialFlowParameters
├── SwirlParameters
├── SecondaryFlowParameters
└── WallParameters
```

This avoids a large flat parameter record and makes additional flow profiles easier to add later.

### Axial flow

\[
a_x = k_x(v_{target}-v_x)
\]

### Swirl

For the default solid-body profile:

\[
v_\theta = \omega r
\]

`SwirlForce` drives the current tangential velocity toward that target.

### Geometry-induced secondary flow

The secondary model works in the local `(u,v)` cross-section frame. Its strength is multiplied by a dimensionless twist factor derived from:

\[
\tau(x) = \frac{d\theta}{dx}
\]

For the current `TwistedPipe`:

\[
\theta(x)=2\pi N\frac{x}{L}, \qquad
\tau=2\pi\frac{N}{L}
\]

The model field is attenuated close to the real cross-section boundary, providing a simple no-slip-like fade:

\[
f(d)=\operatorname{clamp}\left(\frac{d}{d_{fade}},0,1\right)
\]

The resulting local velocity vector is transformed to world space by `LocalFrame`.

## Secondary vector-field view

In **Cross section** view, enable `Show secondary vector field` to display sampled local flow vectors. The `Vector grid` control changes sampling resolution.

The layers are therefore:

```text
actual cross-section boundary
        +
secondary-flow vector field
        +
particle positions
        +
fading particle trails
```

This makes it possible to compare the modeled field with the resulting particle motion.

## Fading trails

Trails remain timestamped and bounded by both sample count and age:

\[
\alpha = \alpha_0\max\left(0,1-\frac{age}{T_{trail}}\right)
\]

With the default two-second lifetime, old segments disappear instead of accumulating into an unreadable mesh.

## Geometry

Available geometries:

- `Circular`
- `Ovoid`
- `TwistedOvoid`

`PipeGeometry` exposes local transforms, signed boundary distance, inward normal, local frame, twist angle and twist rate. Physics does not branch on concrete geometry type.

## Project structure

```text
src/main/scala/io/codeswarm/schauberger/
├── application/
├── benchmark/
├── geometry/
├── math/
├── model/
│   ├── SimulationParameters.scala
│   ├── AxialFlowParameters.scala
│   ├── SwirlParameters.scala
│   ├── SecondaryFlowParameters.scala
│   ├── WallParameters.scala
│   ├── GeometryParameters.scala
│   └── VisualizationParameters.scala
├── physics/
│   ├── AxialFlowForce.scala
│   ├── SwirlForce.scala
│   ├── SecondaryFlowForce.scala
│   ├── WallRepulsionForce.scala
│   └── secondary/
│       ├── SecondaryFlowModel.scala
│       ├── NoSecondaryFlow.scala
│       ├── TwinVortexSecondaryFlow.scala
│       ├── BoundaryAttenuation.scala
│       └── LinearBoundaryAttenuation.scala
├── simulation/
│   ├── SimulationEngine.scala
│   ├── VelocityDecomposer.scala
│   ├── SecondaryFlowFieldSampler.scala
│   └── FlowMetricsCalculator.scala
├── ui/
│   ├── ControlPanel.scala
│   ├── FlowControlPane.scala
│   ├── GeometryControlPane.scala
│   ├── VisualizationControlPane.scala
│   ├── NumericSliderField.scala
│   └── NumericValueCodec.scala
└── visualization/
    ├── VectorFieldRenderer.scala
    ├── TrailRenderer.scala
    ├── ParticleRenderer.scala
    └── PipeRenderer.scala
```

## Architecture rule

```text
ScalaFX UI
    ↓
SimulationController
    ↓
SimulationEngine ────────────── PipeGeometry
    ↓                              ↓
CompositeFlowForce             LocalFrame
    ↓                              ↓
Axial / Swirl / Secondary / Wall  CrossSectionShape
    ↓
math + immutable model
```

`SimulationEngine`, `FlowForce` and geometry code have no dependency on ScalaFX.

## Build and run

Requirements:

- JDK 21
- Gradle

```bash
gradle clean test
gradle run
```

or:

```bash
./scripts/run.sh
```

## Benchmark

```bash
gradle benchmark
```

Particle scenarios:

- `circular-axial`
- `circular-swirl`
- `ovoid-swirl`
- `twisted-ovoid-swirl`
- `twisted-ovoid-swirl-secondary`

Population sizes: 1,000 / 10,000 / 50,000 particles.

Secondary-field sampling is additionally measured at 15x15, 30x30 and 60x60 grid resolutions.

Results are written to:

```text
benchmark/results/benchmark-0.4.0.json
```

The benchmark is intended for regression tracking, not as a replacement for JMH.

## Tests

The suite covers:

- vector mathematics,
- circular and ovoid cross-sections,
- local/world transforms,
- twist rate,
- axial/swirl/wall forces,
- secondary-flow attenuation and symmetry,
- finite secondary-field samples,
- long-running containment invariants,
- enabled-vs-disabled secondary-flow trajectory differences,
- fading trails,
- parameter defaults,
- numeric text parsing/clamping for editable sliders.

## Roadmap

- **0.1** axial particle-flow foundation
- **0.2** 3D swirl and helical trajectories
- **0.3** circular / ovoid / twisted-ovoid geometry
- **0.4** geometry-induced secondary flow + vector field + editable slider values
- **0.5** velocity/vorticity heat maps and field diagnostics
- **0.6** multiple vortex profiles
- **0.7** double / counter-rotating vortex experiments
- **0.8** pressure and energy proxies
- **0.9** comparison/experiment workspace
- **1.0** complete Schauberger-inspired flow laboratory
