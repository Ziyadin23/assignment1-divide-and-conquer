public class MergeSorter {
    private static final int INSERTION_LIMIT = 16;

    public static Metrics sort(int[] values) {
        Metrics metrics = new Metrics();
        if (values.length == 0) {
            return metrics;
        }

        int[] temporary = new int[values.length];
        mergeSort(values, temporary, 0, values.length - 1, 1, metrics);
        return metrics;
    }

    private static void mergeSort(int[] values, int[] temporary, int left, int right,
                                  int depth, Metrics metrics) {
        metrics.enter(depth);

        if (right - left + 1 <= INSERTION_LIMIT) {
            insertionSort(values, left, right, metrics);
            return;
        }

        int middle = left + (right - left) / 2;
        mergeSort(values, temporary, left, middle, depth + 1, metrics);
        mergeSort(values, temporary, middle + 1, right, depth + 1, metrics);

        // The two halves are already in order, so no merge is needed.
        metrics.comparisons++;
        if (values[middle] <= values[middle + 1]) {
            return;
        }

        for (int index = left; index <= right; index++) {
            temporary[index] = values[index];
            metrics.moves++;
        }

        int first = left;
        int second = middle + 1;
        for (int index = left; index <= right; index++) {
            if (first > middle) {
                values[index] = temporary[second++];
            } else if (second > right) {
                values[index] = temporary[first++];
            } else {
                metrics.comparisons++;
                if (temporary[first] <= temporary[second]) {
                    values[index] = temporary[first++];
                } else {
                    values[index] = temporary[second++];
                }
            }
            metrics.moves++;
        }
    }

    private static void insertionSort(int[] values, int left, int right, Metrics metrics) {
        for (int index = left + 1; index <= right; index++) {
            int current = values[index];
            int position = index;
            while (position > left) {
                metrics.comparisons++;
                if (values[position - 1] <= current) {
                    break;
                }
                values[position] = values[position - 1];
                metrics.moves++;
                position--;
            }
            values[position] = current;
            metrics.moves++;
        }
    }
}
