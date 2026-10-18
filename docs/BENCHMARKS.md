# Benchmarks — 0.7.0

## How to reproduce

```bash
./gradlew benchmark   # benchmark/results/benchmark-0.7.0.json
./gradlew jmh         # benchmark/results/jmh-0.7.0.json
```

The application benchmark runs 5 warm-up and 10 measured iterations of 250 steps (100 steps for the 50000-particle
cases) and reports the median and the 95th percentile of an iteration.

## Reference run (0.7.0, author's machine)

Application benchmark and JMH, run with Gradle on JDK 22.0.2 (Corretto), 5 warm-up and 10 measured iterations for the
application benchmark, JMH 1.36 with 1 fork, 5 × 10 s warm-up and 8 × 10 s measurement. There is no 0.6 run on this
machine, so these tables compare features within 0.7; the 0.6 versus 0.7 comparison is further down.

### Simulation scenarios, median ms per 250 steps (10000 particles)

| Scenario | Median ms | p95 ms | Particle updates / s |
|---|---:|---:|---:|
| circular-solid-body | 394.2 | 404.2 | 6.34 M |
| circular-rankine | 407.1 | 421.5 | 6.14 M |
| circular-lamb-oseen | 534.3 | 548.3 | 4.68 M |
| ovoid-solid-body | 720.0 | 741.7 | 3.47 M |
| ovoid-rankine | 733.2 | 750.5 | 3.41 M |
| twisted-ovoid-rankine-no-balance | 882.1 | 888.2 | 2.83 M |
| twisted-ovoid-rankine | 930.5 | 938.4 | 2.69 M |
| twisted-ovoid-counter-rotating | 1046.2 | 1067.4 | 2.39 M |
| twisted-ovoid-rankine-secondary | 1139.6 | 1252.3 | 2.19 M |
| twisted-ovoid-counter-rotating-secondary | 1231.7 | 1240.5 | 2.03 M |

50000 particles, 100 steps: circular-solid-body 969.2 ms, twisted-ovoid-rankine-secondary 2313.1 ms,
twisted-ovoid-counter-rotating-secondary 2482.8 ms.

What the new features cost, on the twisted ovoid at 10000 particles:

| Change | Cost |
|---|---:|
| centripetal balance (Rankine, balance on versus off) | +5.5 % |
| counter-rotating blend instead of Rankine | +12.4 % |
| secondary flow on Rankine | +22.5 % |
| secondary flow on counter-rotating | +17.7 % |

The scenarios are stable: p95 is within about 5 % of the median at 10000 particles except for the secondary-flow Rankine case (10 %); the
1000-particle circular-solid-body run has one slow iteration (p95 64 ms against a 36 ms median). The ordering is as expected: the ovoid roughly doubles the cost of the circular
section, and the twist, the secondary field and the counter-rotating blend each add a modest further step.

### Geometry context (500000 contexts)

| Geometry | Contexts / s |
|---|---:|
| circular | 20.6 M |
| ovoid, exact | 3.75 M |
| ovoid, lookup 512 (default) | 10.78 M |
| ovoid, lookup 1024 | 10.53 M |
| twisted ovoid, exact | 3.65 M |
| twisted ovoid, lookup 512 | 8.20 M |

The 512-sample lookup is 2.9× (ovoid) and 2.2× (twisted ovoid) faster than the exact kernel and indistinguishable from
the 1024-sample table, which supports lowering the default to 512. Lookup accuracy is listed below.

### JMH microbenchmarks

| Benchmark | ops / s | ± error | Note |
|---|---:|---:|---|
| `exactKernel` (radius + normal) | 5.29 M | 0.04 M | |
| `lookupKernel` (two lookups) | 26.37 M | 0.47 M | 5.0× the exact kernel |
| `lookupRay` (one lookup) | 31.61 M | 0.46 M | 1.20× `lookupKernel` |
| `crossSectionSeparate` | 9.81 M | 0.04 M | |
| `crossSectionQuery` | 12.39 M | 0.17 M | 1.26× `crossSectionSeparate` |
| `solidBody` | 1342 M | 17.6 M | |
| `rankineProfile` | 402.9 M | 1.5 M | |
| `lambOseenProfile` | 44.5 M | 0.2 M | |
| `counterRotatingProfile` | 32.8 M | 0.15 M | 1.35× slower than Lamb-Oseen, 12× slower than Rankine |

Reading it:

- The single boundary query does what it was meant to: 26 % more cross-section queries per second than asking for distance
  and normal separately, and 20 % more lookups when radius and normal come from one ray.
- The counter-rotating profile is the most expensive profile (two layers and a `tanh`), but at 33 M evaluations per second
  it is still far below the cost of a particle step; the +12 % in the application benchmark is what it costs in practice.
- Treat the profile figures as per-call throughput under JIT, not as a ranking of real-world cost: the solid-body and
  Rankine benchmarks are simple enough that the JIT can hoist most of the work.

## 0.6 versus 0.7 (single run on a shared container)

### Where these numbers come from

The table below was measured once on a shared 2-core cloud container with a plain `scalac`-compiled build of the
`SimulationBenchmark` main class, 0.6 from `master` and 0.7 from this branch, run one after the other. It shows the
direction and rough size of the changes, not absolute performance: single runs on a shared machine vary by about
±10 % (the 1000-particle circular case below is an example). Please rerun `./gradlew benchmark` on your own machine
before quoting figures; the reference run above is the better source.

### Simulation scenarios, median ms per 250 steps

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

### What the new features cost (container run)

| Scenario | Median ms | Relative |
|---|---:|---:|
| twisted-ovoid-rankine-no-balance | 925.2 | 1.00 |
| twisted-ovoid-rankine (balance on) | 1010.1 | 1.09 |
| twisted-ovoid-counter-rotating | 1123.7 | 1.21 |
| twisted-ovoid-counter-rotating-secondary | 1293.2 | 1.40 |

The centripetal balance costs roughly 9 %, the counter-rotating blend roughly another 11 % (two profile layers and one
`tanh`), and the secondary flow another 15–22 % (0.6 measured 20 % for Rankine). The 50000-particle counter-rotating stress case with secondary
flow ran in 2597.5 ms for 100 steps (0.7 only).

### Geometry context (500000 contexts, container run)

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

Secondary vector-field generation costs 0.10 ms (15 cells per side), 0.28 ms (30) and 0.66 ms (60) in the reference run, unchanged from 0.6. The heat map's
analytic source and temporal averaging run once per frame on a grid of at most 80 × 80 cells and do not appear in the
simulation benchmark.
