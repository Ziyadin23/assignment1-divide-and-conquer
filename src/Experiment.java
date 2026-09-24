import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

public class Experiment {
    private static final int[] SIZES = {200, 2000, 20000};
    private static final String[] TYPES = {"random", "sorted", "reverse", "duplicate-heavy"};
    private static final String[] ALGORITHMS = {
        "MergeSort", "QuickSort", "DeterministicSelect", "ClosestPair"
    };
    private static final int TRIALS = 5;

    public static void run() throws IOException {
        warmUp();
        Files.createDirectories(Path.of("results"));

        try (PrintWriter output = new PrintWriter(Files.newBufferedWriter(
                Path.of("results/results.csv")))) {
            output.println("algorithm,input_type,n,trial,time_ns,max_depth,comparisons,moves,recursive_calls");
            System.out.println("Algorithm             Input type       n       Average ms  Average depth");

            for (String algorithm : ALGORITHMS) {
                for (String type : TYPES) {
                    for (int n : SIZES) {
                        runCase(output, algorithm, type, n);
                    }
                }
            }
        }
        System.out.println("Saved measurements to results/results.csv");
    }

    private static void warmUp() {
        int[] values = makeArray(1000, "random", 0);
        MergeSorter.sort(values.clone());
        QuickSorter.sort(values.clone());
        DeterministicSelector.select(values.clone(), 500, new Metrics());
        ClosestPairSolver.closestDistance(makePoints(1000, "random", 0), new Metrics());
    }

    private static void runCase(PrintWriter output, String algorithm, String type, int n) {
        long totalTime = 0;
        long totalDepth = 0;

        for (int trial = 1; trial <= TRIALS; trial++) {
            Metrics metrics;
            long start;
            long elapsed;

            if (algorithm.equals("ClosestPair")) {
                Point[] points = makePoints(n, type, trial);
                metrics = new Metrics();
                start = System.nanoTime();
                ClosestPairSolver.closestDistance(points, metrics);
                elapsed = System.nanoTime() - start;
            } else {
                int[] values = makeArray(n, type, trial);
                if (algorithm.equals("MergeSort")) {
                    start = System.nanoTime();
                    metrics = MergeSorter.sort(values);
                    elapsed = System.nanoTime() - start;
                } else if (algorithm.equals("QuickSort")) {
                    start = System.nanoTime();
                    metrics = QuickSorter.sort(values);
                    elapsed = System.nanoTime() - start;
                } else {
                    metrics = new Metrics();
                    start = System.nanoTime();
                    DeterministicSelector.select(values, n / 2, metrics);
                    elapsed = System.nanoTime() - start;
                }
            }

            output.printf(Locale.US, "%s,%s,%d,%d,%d,%d,%d,%d,%d%n",
                    algorithm, type, n, trial, elapsed, metrics.maxDepth,
                    metrics.comparisons, metrics.moves, metrics.recursiveCalls);
            totalTime += elapsed;
            totalDepth += metrics.maxDepth;
        }

        System.out.printf(Locale.US, "%-21s %-16s %6d %12.3f %14.1f%n",
                algorithm, type, n, totalTime / (TRIALS * 1_000_000.0),
                totalDepth / (double) TRIALS);
    }

    private static int[] makeArray(int n, String type, int trial) {
        Random random = new Random(2026L + n * 31L + trial * 17L);
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = type.equals("duplicate-heavy")
                    ? random.nextInt(10) : random.nextInt(n * 4 + 1) - n * 2;
        }
        if (type.equals("sorted") || type.equals("reverse")) {
            Arrays.sort(values);
            if (type.equals("reverse")) {
                for (int i = 0; i < n / 2; i++) {
                    int temporary = values[i];
                    values[i] = values[n - 1 - i];
                    values[n - 1 - i] = temporary;
                }
            }
        }
        return values;
    }

    private static Point[] makePoints(int n, String type, int trial) {
        Random random = new Random(937L + n * 23L + trial * 19L);
        Point[] points = new Point[n];
        for (int i = 0; i < n; i++) {
            if (type.equals("duplicate-heavy")) {
                points[i] = new Point(random.nextInt(20), random.nextInt(20));
            } else {
                points[i] = new Point(random.nextInt(100000), random.nextInt(100000));
            }
        }
        if (type.equals("sorted") || type.equals("reverse")) {
            Arrays.sort(points, Comparator.comparingDouble(point -> point.x));
            if (type.equals("reverse")) {
                for (int i = 0; i < n / 2; i++) {
                    Point temporary = points[i];
                    points[i] = points[n - 1 - i];
                    points[n - 1 - i] = temporary;
                }
            }
        }
        return points;
    }
}
