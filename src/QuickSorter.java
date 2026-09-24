import java.util.concurrent.ThreadLocalRandom;

public class QuickSorter {
    public static Metrics sort(int[] values) {
        Metrics metrics = new Metrics();
        if (values.length > 0) {
            quickSort(values, 0, values.length - 1, 1, metrics);
        }
        return metrics;
    }

    private static void quickSort(int[] values, int left, int right,
                                  int depth, Metrics metrics) {
        metrics.enter(depth);

        while (left < right) {
            int pivotIndex = ThreadLocalRandom.current().nextInt(left, right + 1);
            int pivot = values[pivotIndex];
            int smaller = left;
            int index = left;
            int larger = right;

            // Keep smaller values on the left and larger values on the right.
            while (index <= larger) {
                metrics.comparisons++;
                if (values[index] < pivot) {
                    swap(values, smaller, index, metrics);
                    smaller++;
                    index++;
                } else {
                    metrics.comparisons++;
                    if (values[index] > pivot) {
                        swap(values, index, larger, metrics);
                        larger--;
                    } else {
                        index++;
                    }
                }
            }

            // Recurse on the shorter part to keep the call stack small.
            if (smaller - left < right - larger) {
                if (left < smaller - 1) {
                    quickSort(values, left, smaller - 1, depth + 1, metrics);
                }
                left = larger + 1;
            } else {
                if (larger + 1 < right) {
                    quickSort(values, larger + 1, right, depth + 1, metrics);
                }
                right = smaller - 1;
            }
        }
    }

    private static void swap(int[] values, int first, int second, Metrics metrics) {
        if (first == second) {
            return;
        }
        int temporary = values[first];
        values[first] = values[second];
        values[second] = temporary;
        metrics.moves += 2;
    }
}
