package git.folio;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.*;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PerformanceOptimizer {

    private final ListeningExecutorService executorService;
    private final CountDownLatch latch;

    public PerformanceOptimizer(int numThreads) {
        this.executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(numThreads));
        this.latch = new CountDownLatch(2); // Set to the number of concurrent tasks
    }

    public void measureAndOptimizeCriticalOperation() {
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            performCriticalOperation();
        } finally {
            stopwatch.stop();
            System.out.println("Critical operation took: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
        }
    }

    public void performConcurrentTasks() {
        ListenableFuture<String> future1 = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return performTask1();
            }
        });

        ListenableFuture<Integer> future2 = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return performTask2();
            }
        });

        Futures.addCallback(future1, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                System.out.println("Task 1 completed with result: " + result);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println("Task 1 failed: " + t.getMessage());
                latch.countDown();
            }
        }, MoreExecutors.directExecutor());

        Futures.addCallback(future2, new FutureCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                System.out.println("Task 2 completed with result: " + result);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println("Task 2 failed: " + t.getMessage());
                latch.countDown();
            }
        }, MoreExecutors.directExecutor());
    }

    private void performCriticalOperation() {
        // Simulate a time-consuming operation
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String performTask1() {
        // Simulate task 1
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Task 1 Result";
    }

    private int performTask2() {
        // Simulate task 2
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return 42;
    }

    public void shutdown() {
        try {
            // Wait for all tasks to complete
            if (!latch.await(10, TimeUnit.SECONDS)) {
                System.err.println("Timeout waiting for tasks to complete");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for tasks to complete");
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    public static void main(String[] args) {
        PerformanceOptimizer optimizer = new PerformanceOptimizer(4);

        // Measure critical operation
        optimizer.measureAndOptimizeCriticalOperation();

        // Perform concurrent tasks
        optimizer.performConcurrentTasks();

        // Wait for a short time to allow tasks to complete
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Shutdown the executor service
        optimizer.shutdown();
    }
}