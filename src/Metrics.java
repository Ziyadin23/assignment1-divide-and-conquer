public class Metrics {
    public long comparisons;
    public long moves;
    public long recursiveCalls;
    public int maxDepth;

    public void enter(int depth) {
        recursiveCalls++;
        maxDepth = Math.max(maxDepth, depth);
    }
}
