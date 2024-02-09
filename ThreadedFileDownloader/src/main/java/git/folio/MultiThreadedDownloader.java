package git.folio;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLongArray;

public class MultiThreadedDownloader {
    private static final int NUM_THREADS = 8;
    private static final int BUFFER_SIZE = 1024 * 1024; // 1 MB
    private static final Logger logger = LoggerFactory.getLogger(MultiThreadedDownloader.class);
    private static final AtomicLongArray threadProgress = new AtomicLongArray(NUM_THREADS);

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("u", "url", true, "URL of the file to download");
        options.addOption("o", "output", true, "Output file name");
        options.addOption("t", "threads", true, "Number of threads to use");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            String fileUrl = cmd.getOptionValue("url", "https://nodejs.org/download/release/v16.20.2/node-v16.20.2.tar.gz");
            String outputFile = cmd.getOptionValue("output", "node-v16.20.2.tar.gz");
            int numThreads = Integer.parseInt(cmd.getOptionValue("threads", String.valueOf(NUM_THREADS)));

            downloadFile(fileUrl, outputFile, numThreads);
        } catch (ParseException e) {
            logger.error("Failed to parse command line arguments", e);
            System.exit(1);
        } catch (Exception e) {
            logger.error("An error occurred during download", e);
            System.exit(1);
        }
    }

    public static void downloadFile(String fileUrl, String outputFile, int numThreads) throws Exception {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        long fileSize = connection.getContentLengthLong();
        connection.disconnect();

        if (fileSize == -1) {
            logger.error("Unable to determine file size. The server might not support content length header.");
            System.exit(1);
        }

        long partSize = fileSize / numThreads;
        ExecutorService executor = new ThreadPoolExecutor(
                numThreads, numThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        return t;
                    }
                }
        );
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            long startByte = i * partSize;
            long endByte = (i == numThreads - 1) ? fileSize - 1 : (i + 1) * partSize - 1;
            executor.execute(new DownloadTask(fileUrl, outputFile, startByte, endByte, i, latch));
        }

        new Thread(() -> {
            try {
                while (!latch.await(1, TimeUnit.SECONDS)) {
                    printProgress(outputFile, fileSize, numThreads);
                }
                printProgress(outputFile, fileSize, numThreads);
                System.out.println();
            } catch (InterruptedException e) {
                logger.error("Progress thread interrupted", e);
            }
        }).start();

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        logger.info("Download complete!");
    }

    private static void printProgress(String outputFile, long totalSize, int numThreads) {
        File file = new File(outputFile);
        long downloadedSize = file.length();
        int progress = (int) ((downloadedSize * 100) / totalSize);
        System.out.print("\rProgress: [" + "=".repeat(progress) + " ".repeat(100 - progress) + "] " + progress + "%");

        System.out.println();
        for (int i = 0; i < numThreads; i++) {
            long threadBytes = threadProgress.get(i);
            System.out.printf("Thread %d: %d bytes%n", i, threadBytes);
        }
    }

    static class DownloadTask implements Runnable {
        private String fileUrl;
        private String outputFile;
        private long startByte;
        private long endByte;
        private int threadId;
        private CountDownLatch latch;

        public DownloadTask(String fileUrl, String outputFile, long startByte, long endByte, int threadId, CountDownLatch latch) {
            this.fileUrl = fileUrl;
            this.outputFile = outputFile;
            this.startByte = startByte;
            this.endByte = endByte;
            this.threadId = threadId;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                logger.info("Thread {} starting download", threadId);
                URL url = new URL(fileUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                    try (InputStream in = connection.getInputStream();
                         RandomAccessFile raf = new RandomAccessFile(outputFile, "rw")) {

                        raf.seek(startByte);
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;
                        long bytesDownloaded = 0;

                        while ((bytesRead = in.read(buffer)) != -1) {
                            raf.write(buffer, 0, bytesRead);
                            bytesDownloaded += bytesRead;
                            threadProgress.set(threadId, bytesDownloaded);
                        }
                    }
                    logger.info("Thread {} finished downloading", threadId);
                } else {
                    logger.error("Thread {} failed: Server doesn't support range requests", threadId);
                }
            } catch (IOException e) {
                logger.error("Error in thread {}", threadId, e);
            } finally {
                latch.countDown();
            }
        }
    }
}