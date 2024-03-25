package git.folio;

import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class BackgroundTaskScheduler {

    private final ScheduledExecutorService scheduler;
    private final AtomicInteger taskCounter = new AtomicInteger(0);

    public BackgroundTaskScheduler(int poolSize) {
        this.scheduler = Executors.newScheduledThreadPool(poolSize);
    }

    public void scheduleOneTimeTask(Runnable task, long delay, TimeUnit timeUnit) {
        int taskId = taskCounter.incrementAndGet();
        scheduler.schedule(() -> {
            System.out.println("Executing one-time task " + taskId + " at " + LocalDateTime.now());
            task.run();
        }, delay, timeUnit);
        System.out.println("Scheduled one-time task " + taskId + " to run after " + delay + " " + timeUnit);
    }

    public void scheduleRecurringTask(Runnable task, long initialDelay, long period, TimeUnit timeUnit) {
        int taskId = taskCounter.incrementAndGet();
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Executing recurring task " + taskId + " at " + LocalDateTime.now());
            task.run();
        }, initialDelay, period, timeUnit);
        System.out.println("Scheduled recurring task " + taskId + " to run every " + period + " " + timeUnit);
    }

    public void shutdown() {
        System.out.println("Shutting down scheduler");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    public static void main(String[] args) {
        BackgroundTaskScheduler scheduler = new BackgroundTaskScheduler(2);

        // Schedule a one-time task
        scheduler.scheduleOneTimeTask(() -> {
            System.out.println("One-time task executed");
        }, 5, TimeUnit.SECONDS);

        // Schedule a recurring task
        scheduler.scheduleRecurringTask(() -> {
            System.out.println("Recurring task executed");
        }, 0, 3, TimeUnit.SECONDS);

        // Let tasks run for a while
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Shutdown the scheduler
        scheduler.shutdown();
    }
}