import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
        int[] original = {7, 2, 9, 2, 1, 5};
        int[] merge = original.clone();
        int[] quick = original.clone();

        MergeSorter.sort(merge);
        QuickSorter.sort(quick);
        int third = DeterministicSelector.select(original.clone(), 2, new Metrics());
        Point[] points = {
            new Point(0, 0), new Point(4, 3), new Point(1, 1), new Point(8, 9)
        };
        double distance = ClosestPairSolver.closestDistance(points, new Metrics());

        System.out.println("Divide-and-Conquer Assignment 1");
        System.out.println("MergeSort: " + Arrays.toString(merge));
        System.out.println("QuickSort: " + Arrays.toString(quick));
        System.out.println("3rd smallest: " + third);
        System.out.println("Closest distance: " + distance);
        System.out.println();

        Experiment.run();
    }
}
