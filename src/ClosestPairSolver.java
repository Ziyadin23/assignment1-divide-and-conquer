public class ClosestPairSolver {
    public static double closestDistance(Point[] points, Metrics metrics) {
        if (points == null) {
            throw new IllegalArgumentException("Points array cannot be null");
        }
        if (points.length < 2) {
            return Double.POSITIVE_INFINITY;
        }

        int n = points.length;
        Point[] byX = new Point[n];
        Point[] byY = new Point[n];
        Point[] buffer = new Point[n];
        for (int i = 0; i < n; i++) {
            if (points[i] == null) {
                throw new IllegalArgumentException("Point cannot be null");
            }
            byX[i] = points[i];
            if (metrics != null) {
                metrics.moves++;
            }
        }
        sortByX(byX, buffer, 0, n, metrics, 1);
        for (int i = 0; i < n; i++) {
            byY[i] = byX[i];
            if (metrics != null) {
                metrics.moves++;
            }
        }

        return solve(byX, byY, buffer, 0, n, metrics, 1);
    }

    // Merge sort keeps the initial x order available to every recursive call.
    private static void sortByX(Point[] points, Point[] buffer, int left, int right,
                                Metrics metrics, int depth) {
        if (metrics != null) {
            metrics.enter(depth);
        }
        if (right - left <= 1) {
            return;
        }
        int middle = (left + right) / 2;
        sortByX(points, buffer, left, middle, metrics, depth + 1);
        sortByX(points, buffer, middle, right, metrics, depth + 1);

        int i = left;
        int j = middle;
        for (int p = left; p < right; p++) {
            if (j == right || (i < middle && compareByX(points[i], points[j]) <= 0)) {
                buffer[p] = points[i++];
            } else {
                buffer[p] = points[j++];
            }
            if (metrics != null) {
                metrics.moves++;
            }
        }
        for (int p = left; p < right; p++) {
            points[p] = buffer[p];
            if (metrics != null) {
                metrics.moves++;
            }
        }
    }

    private static int compareByX(Point a, Point b) {
        int xResult = Double.compare(a.x, b.x);
        return xResult != 0 ? xResult : Double.compare(a.y, b.y);
    }

    private static double solve(Point[] byX, Point[] byY, Point[] buffer,
                                int left, int right, Metrics metrics, int depth) {
        if (metrics != null) {
            metrics.enter(depth);
        }

        int length = right - left;
        if (length <= 3) {
            double best = Double.POSITIVE_INFINITY;
            for (int i = left; i < right; i++) {
                for (int j = i + 1; j < right; j++) {
                    best = Math.min(best, distance(byY[i], byY[j], metrics));
                }
            }
            sortSmallRangeByY(byY, left, right, metrics);
            return best;
        }

        int middle = (left + right) / 2;
        double middleX = byX[middle].x;
        double leftBest = solve(byX, byY, buffer, left, middle, metrics, depth + 1);
        double rightBest = solve(byX, byY, buffer, middle, right, metrics, depth + 1);
        double best = Math.min(leftBest, rightBest);

        // The two halves are already sorted by y. Merge them for this call's strip.
        int i = left;
        int j = middle;
        for (int p = left; p < right; p++) {
            if (j == right || (i < middle && compareByY(byY[i], byY[j]) <= 0)) {
                buffer[p] = byY[i++];
            } else {
                buffer[p] = byY[j++];
            }
            if (metrics != null) {
                metrics.moves++;
            }
        }
        for (int p = left; p < right; p++) {
            byY[p] = buffer[p];
            if (metrics != null) {
                metrics.moves++;
            }
        }

        // There can be at most seven later points to check for each strip point.
        int stripSize = 0;
        for (int p = left; p < right; p++) {
            if (Math.abs(byY[p].x - middleX) < best) {
                buffer[stripSize++] = byY[p];
                if (metrics != null) {
                    metrics.moves++;
                }
            }
        }
        for (int p = 0; p < stripSize; p++) {
            for (int q = p + 1; q < stripSize && q <= p + 7; q++) {
                if (buffer[q].y - buffer[p].y >= best) {
                    break;
                }
                best = Math.min(best, distance(buffer[p], buffer[q], metrics));
            }
        }
        return best;
    }

    private static int compareByY(Point a, Point b) {
        int yResult = Double.compare(a.y, b.y);
        return yResult != 0 ? yResult : Double.compare(a.x, b.x);
    }

    private static void sortSmallRangeByY(Point[] points, int left, int right,
                                          Metrics metrics) {
        for (int i = left + 1; i < right; i++) {
            Point item = points[i];
            int j = i - 1;
            while (j >= left && compareByY(points[j], item) > 0) {
                points[j + 1] = points[j];
                if (metrics != null) {
                    metrics.moves++;
                }
                j--;
            }
            points[j + 1] = item;
            if (metrics != null) {
                metrics.moves++;
            }
        }
    }

    private static double distance(Point a, Point b, Metrics metrics) {
        if (metrics != null) {
            metrics.comparisons++;
        }
        return Math.hypot(a.x - b.x, a.y - b.y);
    }
}
