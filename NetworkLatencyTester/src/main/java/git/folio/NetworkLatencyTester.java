package git.folio;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NetworkLatencyTester {
    private static final int NUM_THREADS = 5;
    private static final int LOOPS_PER_THREAD = 50;
    private static final String TARGET_URL = "http://www.example.com";

    public static void main(String[] args) {
        NetworkLatencyTester tester = new NetworkLatencyTester();
        tester.runTest();
    }

    public void runTest() {
        System.out.println("Testing connection to " + TARGET_URL);
        System.out.println("Threads: " + NUM_THREADS);
        System.out.println("Loops per thread: " + LOOPS_PER_THREAD);

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        List<Future<List<Integer>>> futures = new ArrayList<>();

        for (int i = 0; i < NUM_THREADS; i++) {
            futures.add(executor.submit(new LatencyTestTask()));
        }

        List<Integer> allTimes = new ArrayList<>();
        int failures = 0;

        for (Future<List<Integer>> future : futures) {
            try {
                List<Integer> times = future.get();
                allTimes.addAll(times);
                failures += times.stream().filter(time -> time == -1).count();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        double averageTime = allTimes.stream().filter(time -> time != -1).mapToInt(Integer::intValue).average().orElse(0);
        double failureRate = (double) failures / (NUM_THREADS * LOOPS_PER_THREAD) * 100;

        System.out.printf("Average time to connection: %.2f [ms]\n", averageTime);
        System.out.printf("Failures: %.1f%%\n", failureRate);
    }

    private class LatencyTestTask implements Callable<List<Integer>> {
        @Override
        public List<Integer> call() {
            List<Integer> times = new ArrayList<>();
            for (int i = 0; i < LOOPS_PER_THREAD; i++) {
                times.add(measureLatency());
            }
            return times;
        }

        private int measureLatency() {
            long startTime = System.currentTimeMillis();
            try {
                URL url = new URL(TARGET_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    // Read the first line to ensure the connection is established
                    reader.readLine();
                }

                connection.disconnect();
                return (int) (System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                return -1; // Indicate failure
            }
        }
    }
}