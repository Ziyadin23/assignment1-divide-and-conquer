public class DeterministicSelector {
    // k is a zero-based position in the sorted order.
    public static int select(int[] values, int k, Metrics metrics) {
        if (values == null || k < 0 || k >= values.length) {
            throw new IllegalArgumentException("Invalid array or k");
        }
        return selectRange(values, 0, values.length - 1, k, metrics, 1);
    }

    private static int selectRange(int[] values, int left, int right,
                                   int k, Metrics metrics, int depth) {
        if (metrics != null) {
            metrics.enter(depth);
        }

        if (right - left < 5) {
            insertionSort(values, left, right, metrics);
            return values[k];
        }

        // Put each group's median at the start of this range.
        int medianCount = 0;
        for (int start = left; start <= right; start += 5) {
            int end = Math.min(start + 4, right);
            insertionSort(values, start, end, metrics);
            int medianIndex = start + (end - start) / 2;
            swap(values, left + medianCount, medianIndex, metrics);
            medianCount++;
        }

        // Find the median of the group medians in the same deterministic way.
        int pivotIndex = left + medianCount / 2;
        int pivot = selectRange(values, left, left + medianCount - 1,
                                pivotIndex, metrics, depth + 1);

        // Values in [left, low) are smaller; values in (high, right] are larger.
        // The middle section is equal to the pivot, so duplicates are handled at once.
        int low = left;
        int current = left;
        int high = right;
        while (current <= high) {
            int comparison = Integer.compare(values[current], pivot);
            if (metrics != null) {
                metrics.comparisons++;
            }
            if (comparison < 0) {
                swap(values, low, current, metrics);
                low++;
                current++;
            } else if (comparison > 0) {
                swap(values, current, high, metrics);
                high--;
            } else {
                current++;
            }
        }

        if (k < low) {
            return selectRange(values, left, low - 1, k, metrics, depth + 1);
        }
        if (k > high) {
            return selectRange(values, high + 1, right, k, metrics, depth + 1);
        }
        return pivot;
    }

    private static void insertionSort(int[] values, int left, int right,
                                      Metrics metrics) {
        for (int i = left + 1; i <= right; i++) {
            int item = values[i];
            int j = i - 1;
            while (j >= left) {
                if (metrics != null) {
                    metrics.comparisons++;
                }
                if (values[j] <= item) {
                    break;
                }
                values[j + 1] = values[j];
                if (metrics != null) {
                    metrics.moves++;
                }
                j--;
            }
            values[j + 1] = item;
            if (metrics != null) {
                metrics.moves++;
            }
        }
    }

    private static void swap(int[] values, int i, int j, Metrics metrics) {
        if (i == j) {
            return;
        }
        int temporary = values[i];
        values[i] = values[j];
        values[j] = temporary;
        if (metrics != null) {
            metrics.moves += 2;
        }
    }
}
