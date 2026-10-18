# Benchmarks — 0.6 versus 0.7

## How to reproduce

```bash
./gradlew benchmark   # benchmark/results/benchmark-0.7.0.json
./gradlew jmh         # benchmark/results/jmh-0.7.0.json
```

The application benchmark runs 5 warm-up and 10 measured iterations of 250 steps (100 steps for the 50000-particle
cases) and reports the median and the 95th percentile of an iteration.

## Where these numbers come from

The table below was measured once on a shared 2-core cloud container with a plain `scalac`-compiled build of the
`SimulationBenchmark` main class, 0.6 from `master` and 0.7 from this branch, run one after the other. It shows the
direction and rough size of the changes, not absolute performance: single runs on a shared machine vary by about
±10 % (the 1000-particle circular case below is an example). Please rerun `./gradlew benchmark` on your own machine
before quoting figures. JMH results were not produced for this release.

## Simulation scenarios, median ms per 250 steps

| Scenario | Particles | 0.6 | 0.7 | 0.7 / 0.6 |
|---|---:|---:|---:|---:|
| circular-solid-body | 10000 | 494.5 | 458.8 | 0.93 |
| circular-rankine | 10000 | 438.6 | 502.5 | 1.15 |
| circular-lamb-oseen | 10000 | 600.0 | 547.5 | 0.91 |
| ovoid-solid-body | 10000 | 985.3 | 781.1 | 0.79 |
| ovoid-rankine | 10000 | 959.9 | 758.3 | 0.79 |
| twisted-ovoid-rankine | 10000 | 1233.1 | 1010.1 | 0.82 |
| twisted-ovoid-rankine-secondary | 10000 | 1481.2 | 1232.4 | 0.83 |
| twisted-ovoid-rankine-secondary | 50000 (100 steps) | 3008.1 | 2432.1 | 0.81 |

Reading it:

- **Ovoid and twisted-ovoid scenarios are about 20 % faster**, although 0.7 does more work per particle (the centripetal
  balance and, for counter-rotating flow, a second profile layer). The gain comes from evaluating the boundary once per
  particle and from the array-backed lookup.
- **Circular scenarios do not use the ovoid lookup** and stay within measurement noise (0.91–1.15). The one scenario above
  1.0 is a 10000-particle Rankine run; the 1000-particle runs of the same scenario are 0.94, so treat these as noise.

## What the new features cost (0.7, 10000 particles, twisted ovoid)

| Scenario | Median ms | Relative |
|---|---:|---:|
| twisted-ovoid-rankine-no-balance | 925.2 | 1.00 |
| twisted-ovoid-rankine (balance on) | 1010.1 | 1.09 |
| twisted-ovoid-counter-rotating | 1123.7 | 1.21 |
| twisted-ovoid-counter-rotating-secondary | 1293.2 | 1.40 |

The centripetal balance costs roughly 9 %, the counter-rotating blend roughly another 11 % (two profile layers and one
`tanh`), and the secondary flow another 15–22 % (0.6 measured 20 % for Rankine). The 50000-particle counter-rotating stress case with secondary
flow ran in 2597.5 ms for 100 steps (0.7 only).

## Geometry context (500000 contexts)

| Geometry | 0.6 contexts/s | 0.7 contexts/s | 0.7 / 0.6 |
|---|---:|---:|---:|
| circular | 20.1 M | 20.8 M | 1.04 |
| ovoid, exact | 2.89 M | 3.05 M | 1.05 |
| ovoid, lookup 1024 | 5.75 M | 7.87 M | 1.37 |
| ovoid, lookup 512 (0.7 default) | — | 8.90 M | 1.55 against 0.6 / 1024 |
| twisted ovoid, lookup 1024 → 512 | 5.12 M | 7.27 M | 1.42 |

The context is 1.4–1.55× faster on the lookup path. Exact mode barely changes because its cost is the analytic
evaluation, which a single query does not remove.

## Lookup accuracy

Unchanged by the optimisation (the maths is the same); the default simply moved from 1024 to 512 entries:

| Samples | Max relative radius error | Max normal error (degrees) |
|---:|---:|---:|
| 256 | 3.1e-5 | 0.0026 |
| 512 (default) | 7.8e-6 | 0.00064 |
| 1024 (0.6 default) | 1.9e-6 | 0.00016 |
| 2048 | 4.9e-7 | 0.00004 |

## Field sampling

Secondary vector-field generation is unchanged in cost (0.09–0.7 ms median for 15–60 cells per side). The heat map's
analytic source and temporal averaging run once per frame on a grid of at most 80 × 80 cells and do not appear in the
simulation benchmark.

## JMH additions

`OvoidGeometryBenchmark` now also measures `lookupRay` (radius and normal from one lookup), `crossSectionSeparate` and
`crossSectionQuery`; `VortexProfileBenchmark` adds `counterRotatingProfile`. Run `./gradlew jmh` to compare them.
