import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class AlgorithmTest {
    @Test
    void sortingMatchesArraysSort() {
        int[] random = new int[1000];
        Random generator = new Random(123);
        for (int i = 0; i < random.length; i++) {
            random[i] = generator.nextInt(2001) - 1000;
        }

        int[] sorted = new int[100];
        int[] reverse = new int[100];
        for (int i = 0; i < 100; i++) {
            sorted[i] = i - 50;
            reverse[i] = 50 - i;
        }

        checkBothSorts(random);
        checkBothSorts(sorted);
        checkBothSorts(reverse);
        checkBothSorts(new int[] {5, 1, 5, 2, 5, 1, 5, 2, 5});
        checkBothSorts(new int[] {7, 7, 7, 7, 7});
        checkBothSorts(new int[0]);
        checkBothSorts(new int[] {42});
    }

    @Test
    void selectMatchesSortedArrayInAtLeast100RandomTests() {
        Random generator = new Random(456);
        for (int test = 0; test < 150; test++) {
            int size = 1 + generator.nextInt(150);
            int[] values = new int[size];
            for (int i = 0; i < size; i++) {
                // The small range also makes duplicate values common.
                values[i] = generator.nextInt(41) - 20;
            }
            int k = generator.nextInt(size);
            int[] expected = values.clone();
            Arrays.sort(expected);
            assertEquals(expected[k], DeterministicSelector.select(values, k, new Metrics()),
                    "random test " + test);
        }
    }

    @Test
    void selectHandlesDuplicatesAndEndPositions() {
        int[] values = {8, -3, 8, 4, -3, 8, 0};
        assertEquals(-3, DeterministicSelector.select(values.clone(), 0, new Metrics()));
        assertEquals(8, DeterministicSelector.select(values.clone(), values.length - 1,
                new Metrics()));
        assertEquals(8, DeterministicSelector.select(new int[] {8, 8, 8, 8}, 2,
                new Metrics()));
        assertEquals(9, DeterministicSelector.select(new int[] {9}, 0, new Metrics()));
    }

    @Test
    void selectRejectsInvalidPositions() {
        assertThrows(IllegalArgumentException.class,
                () -> DeterministicSelector.select(new int[] {1, 2, 3}, -1, new Metrics()));
        assertThrows(IllegalArgumentException.class,
                () -> DeterministicSelector.select(new int[] {1, 2, 3}, 3, new Metrics()));
        assertThrows(IllegalArgumentException.class,
                () -> DeterministicSelector.select(new int[0], 0, new Metrics()));
    }

    @Test
    void closestPairMatchesBruteForceForSmallSets() {
        Random generator = new Random(789);
        for (int test = 0; test < 50; test++) {
            int size = 2 + generator.nextInt(100);
            Point[] points = new Point[size];
            for (int i = 0; i < size; i++) {
                points[i] = new Point(generator.nextInt(201) - 100,
                        generator.nextInt(201) - 100);
            }
            double expected = bruteForceDistance(points);
            assertEquals(expected, ClosestPairSolver.closestDistance(points, new Metrics()),
                    1e-9, "random test " + test);
        }

        // The largest brute-force comparison stays within the assignment's n <= 2,000 rule.
        Point[] twoThousand = new Point[2000];
        for (int i = 0; i < twoThousand.length; i++) {
            twoThousand[i] = new Point(i * 3, i % 17);
        }
        assertEquals(bruteForceDistance(twoThousand),
                ClosestPairSolver.closestDistance(twoThousand, new Metrics()), 1e-9);
    }

    @Test
    void closestPairHandlesDuplicateAndSmallInputs() {
        Point[] duplicates = {
            new Point(2, 3), new Point(-1, 0), new Point(2, 3)
        };
        assertEquals(0.0, ClosestPairSolver.closestDistance(duplicates, new Metrics()), 1e-9);
        assertTrue(Double.isInfinite(ClosestPairSolver.closestDistance(new Point[0],
                new Metrics())));
        assertTrue(Double.isInfinite(ClosestPairSolver.closestDistance(
                new Point[] {new Point(1, 2)}, new Metrics())));
    }

    @Test
    void closestPairRunsOnLargeInput() {
        Point[] points = new Point[10000];
        points[0] = new Point(0, 0);
        points[1] = new Point(0, 0.00001);
        for (int i = 2; i < points.length; i++) {
            points[i] = new Point(i * 10, (i % 100) * 10);
        }
        assertEquals(0.00001, ClosestPairSolver.closestDistance(points, new Metrics()), 1e-9);
    }

    private void checkBothSorts(int[] input) {
        int[] expected = input.clone();
        Arrays.sort(expected);

        int[] mergeResult = input.clone();
        MergeSorter.sort(mergeResult);
        assertArrayEquals(expected, mergeResult, "MergeSort");

        int[] quickResult = input.clone();
        QuickSorter.sort(quickResult);
        assertArrayEquals(expected, quickResult, "QuickSort");
    }

    private double bruteForceDistance(Point[] points) {
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i < points.length; i++) {
            for (int j = i + 1; j < points.length; j++) {
                double distance = Math.hypot(points[i].x - points[j].x,
                        points[i].y - points[j].y);
                best = Math.min(best, distance);
            }
        }
        return best;
    }
}
