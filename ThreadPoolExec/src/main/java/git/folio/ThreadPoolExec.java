package git.folio;

import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

public class ThreadPoolExec {

    private final int corePoolSize;
    private final int maximumPoolSize;
    private final long keepAliveTime;
    private final TimeUnit unit;
    private final BlockingQueue<Runnable> workQueue;
    private final ThreadFactory threadFactory;
    private final CustomRejectedExecutionHandler handler;
    private final List<WorkerThread> workers;
    private boolean isShutdown;

    public ThreadPoolExec(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          CustomRejectedExecutionHandler handler) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.workQueue = workQueue;
        this.threadFactory = threadFactory;
        this.handler = handler;
        this.workers = new ArrayList<>();
        this.isShutdown = false;
    }

    public void execute(Runnable command) {
        if (isShutdown) {
            throw new RejectedExecutionException("ThreadPool is shutdown");
        }

        if (workers.size() < corePoolSize) {
            addWorker(command);
        } else if (workQueue.offer(command)) {
            // Task added to queue successfully
        } else if (workers.size() < maximumPoolSize) {
            addWorker(command);
        } else {
            handler.rejectedExecution(command, this);
        }
    }

    private void addWorker(Runnable firstTask) {
        WorkerThread worker = new WorkerThread(firstTask);
        workers.add(worker);
        Thread t = threadFactory.newThread(worker);
        t.start();
    }

    public void shutdown() {
        isShutdown = true;
        for (WorkerThread worker : workers) {
            worker.interrupt();
        }
    }

    public List<Runnable> shutdownNow() {
        shutdown();
        List<Runnable> remainingTasks = new ArrayList<>();
        workQueue.drainTo(remainingTasks);
        return remainingTasks;
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    public boolean isTerminated() {
        return isShutdown && workers.isEmpty();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        long deadline = System.nanoTime() + nanos;
        synchronized (this) {
            while (!isTerminated()) {
                if (nanos <= 0) {
                    return false;
                }
                wait(nanos / 1_000_000, (int) (nanos % 1_000_000));
                nanos = deadline - System.nanoTime();
            }
        }
        return true;
    }

    private class WorkerThread implements Runnable {
        private Runnable firstTask;

        public WorkerThread(Runnable firstTask) {
            this.firstTask = firstTask;
        }

        @Override
        public void run() {
            try {
                Runnable task = firstTask;
                firstTask = null;
                while (task != null || (task = getTask()) != null) {
                    try {
                        task.run();
                    } finally {
                        task = null;
                    }
                }
            } finally {
                workers.remove(this);
                synchronized (ThreadPoolExec.this) {
                    ThreadPoolExec.this.notifyAll();
                }
            }
        }

        private Runnable getTask() {
            try {
                if (workers.size() > corePoolSize) {
                    return workQueue.poll(keepAliveTime, unit);
                } else {
                    return workQueue.take();
                }
            } catch (InterruptedException e) {
                return null;
            }
        }

        public void interrupt() {
            Thread.currentThread().interrupt();
        }
    }

    // Custom RejectedExecutionHandler interface
    public interface CustomRejectedExecutionHandler {
        void rejectedExecution(Runnable r, ThreadPoolExec executor);
    }

    public static void main(String[] args) {
        ThreadPoolExec executor = new ThreadPoolExec(
                5,                                  // corePoolSize
                10,                                  // maximumPoolSize
                60L,                                // keepAliveTime
                TimeUnit.SECONDS,                   // unit
                new LinkedBlockingQueue<>(10),      // workQueue
                Executors.defaultThreadFactory(),   // threadFactory
                (r, e) -> System.out.println("Task rejected: " + r.toString())  // CustomRejectedExecutionHandler
        );

        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            executor.execute(() -> {
                System.out.println("Task " + taskId + " executed by " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000); // Simulate some work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("All tasks completed.");
    }
}