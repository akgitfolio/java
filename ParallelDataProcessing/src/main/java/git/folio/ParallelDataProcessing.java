package git.folio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ParallelDataProcessing {

    public static void main(String[] args) {
        // Reduce dataset size for quicker execution
        List<Integer> dataset = createLargeDataset(100_000);

        System.out.println("Starting sequential processing...");
        long startTimeSeq = System.nanoTime();
        long sumSeq = processSequentially(dataset);
        long endTimeSeq = System.nanoTime();

        System.out.println("Starting parallel processing...");
        long startTimePar = System.nanoTime();
        long sumPar = processInParallel(dataset);
        long endTimePar = System.nanoTime();

        // Print results
        System.out.println("Sequential sum: " + sumSeq);
        System.out.println("Parallel sum: " + sumPar);

        System.out.println("Sequential processing time: " +
                TimeUnit.NANOSECONDS.toMillis(endTimeSeq - startTimeSeq) + " ms");
        System.out.println("Parallel processing time: " +
                TimeUnit.NANOSECONDS.toMillis(endTimePar - startTimePar) + " ms");
    }

    private static List<Integer> createLargeDataset(int size) {
        List<Integer> dataset = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            dataset.add(i);
        }
        return dataset;
    }

    private static long processSequentially(List<Integer> dataset) {
        return dataset.stream()
                .mapToLong(ParallelDataProcessing::expensiveOperation)
                .sum();
    }

    private static long processInParallel(List<Integer> dataset) {
        return dataset.parallelStream()
                .mapToLong(ParallelDataProcessing::expensiveOperation)
                .sum();
    }

    private static long expensiveOperation(int value) {
        // Simulate a less expensive operation
        return value * value;
    }
}