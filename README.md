# Assignment 1: Divide and Conquer

I implemented MergeSort, QuickSort, Deterministic Select, and Closest Pair in Java, then measured time, recursion depth, and operations.

## Run

Use Java 17+ and Maven:

```bash
mvn test
mvn package
java -jar target/assignment1-divide-and-conquer-1.0.0.jar
```

The program prints results and saves [results/results.csv](results/results.csv). Tests use `Arrays.sort` for sorting and 150 selection cases, and brute force for Closest Pair (up to 2,000 points). They cover empty, single-item, and duplicate inputs.

## Algorithms

**MergeSort:** Sort two halves, then merge with a reusable buffer. Use insertion sort for at most 16 items and skip unnecessary merges. `T(n) = 2T(n/2) + Θ(n)` gives `Θ(n log n)` time by the Master Theorem. Space: `Θ(n)` buffer and `Θ(log n)` stack.

**QuickSort:** Pick a random pivot; partition in place into smaller, equal, and larger values. Recurse on the smaller part and loop over the larger. Balanced splits give `T(n) ≈ 2T(n/2) + Θ(n) = Θ(n log n)` by the Master Theorem. Bad pivots give `T(n) = T(n-1) + Θ(n) = Θ(n²)`. Space: `O(log n)` stack, `O(1)` partition.

**Deterministic Select:** Sort groups of five, pick their median of medians, and partition in place. Search only the part containing `k`. `T(n) ≤ T(n/5) + T(7n/10) + Θ(n)` is `Θ(n)` in the worst case: the fractions total less than 1 (Akra-Bazzi idea). Space: `O(log n)` stack, `O(1)` extra array space.

**Closest Pair:** Sort by x, solve both halves, merge y-order, and check at most seven neighbors per point in the middle strip. `T(n) = 2T(n/2) + Θ(n)` gives `Θ(n log n)` by the Master Theorem; x-sorting has the same bound. Space: `Θ(n)` arrays, `Θ(log n)` stack.

## Experiments

I tested 200, 2,000, and 20,000 items: random, sorted, reverse-sorted, and duplicate-heavy. Selection uses `k = n/2`. Duplicate-heavy arrays have 10 values; points use a 20 × 20 grid. After warm-up, I ran five trials per case. `System.nanoTime()` measures only the algorithm. The CSV includes comparisons, moves, and calls. For Closest Pair, comparisons are distance checks and depth includes x-sorting.

Each cell is **average milliseconds / average maximum recursion depth** over five trials. Measured with OpenJDK 25.0.4.1 on Ryzen 7 5800H.

| Algorithm | Input | 200 | 2,000 | 20,000 |
|---|---|---:|---:|---:|
| MergeSort | random | 0.0786 / 5.0 | 0.3349 / 8.0 | 5.0908 / 12.0 |
| MergeSort | sorted | 0.0080 / 5.0 | 0.0233 / 8.0 | 0.2432 / 12.0 |
| MergeSort | reverse | 0.0184 / 5.0 | 0.3477 / 8.0 | 3.6857 / 12.0 |
| MergeSort | duplicate-heavy | 0.0229 / 5.0 | 0.3668 / 8.0 | 4.2803 / 12.0 |
| QuickSort | random | 0.0431 / 4.4 | 0.4243 / 7.0 | 5.7456 / 8.8 |
| QuickSort | sorted | 0.0271 / 4.6 | 0.4187 / 7.0 | 2.5056 / 9.0 |
| QuickSort | reverse | 0.0136 / 4.0 | 0.1761 / 7.0 | 1.8595 / 9.2 |
| QuickSort | duplicate-heavy | 0.0076 / 2.2 | 0.0626 / 2.0 | 0.6094 / 2.0 |
| Deterministic Select | random | 0.1068 / 6.2 | 0.3252 / 10.0 | 2.5143 / 13.0 |
| Deterministic Select | sorted | 0.0113 / 6.8 | 0.1098 / 9.8 | 1.1053 / 13.0 |
| Deterministic Select | reverse | 0.0127 / 6.6 | 0.1310 / 9.8 | 1.3612 / 13.2 |
| Deterministic Select | duplicate-heavy | 0.0111 / 4.4 | 0.0838 / 5.4 | 0.9332 / 7.6 |
| Closest Pair | random | 0.5078 / 9.0 | 4.5808 / 12.0 | 19.6097 / 16.0 |
| Closest Pair | sorted | 0.0556 / 9.0 | 0.6361 / 12.0 | 13.2281 / 16.0 |
| Closest Pair | reverse | 0.0534 / 9.0 | 0.6337 / 12.0 | 9.6725 / 16.0 |
| Closest Pair | duplicate-heavy | 0.1365 / 9.0 | 0.8263 / 12.0 | 11.1990 / 16.0 |

![Time vs input size](docs/plots/time_vs_n.png)

![Recursion depth vs input size](docs/plots/depth_vs_n.png)

## Discussion

1. **Does theory match?** Generally yes, though short timings are noisy.
2. **Does input structure matter?** Yes. At 20,000 items, sorted MergeSort took 0.2432 ms versus 5.0908 ms for random input. Three-way QuickSort handles duplicates well.
3. **Why recurse on QuickSort's smaller part?** Each call handles at most half; looping over the rest keeps stack depth `O(log n)`.
4. **Why is Median-of-Medians linear?** Groups of five discard a fixed fraction, leaving at most about 70%; total work is `O(n)`.
5. **Why use divide and conquer for Closest Pair?** Brute force checks about 200 million pairs at 20,000 points. The strip checks only nearby points.
6. **What affects timings?** JVM warm-up, JIT, cache, GC, random pivots, and other computer activity.

## Reflection

Measuring time and depth helped me understand the recurrences. I also saw why input order matters. The hardest parts were duplicates in selection and y-order in Closest Pair. `Arrays.sort` and brute force helped me check my code.

## Screenshots

The first two show saved run logs in kitty.

Program output:

![Program output](docs/screenshots/program_output.png)

Test results:

![Test results](docs/screenshots/test_results.png)

Plots and CSV:

![Plots and results](docs/screenshots/plots_and_results.png)
