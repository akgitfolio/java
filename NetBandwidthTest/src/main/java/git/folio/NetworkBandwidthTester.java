package git.folio;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class NetworkBandwidthTester {

    public static void main(String[] args) throws InterruptedException {
        SpeedTestSocket speedTestSocket = new SpeedTestSocket();
        AtomicReference<CountDownLatch> latchRef = new AtomicReference<>(new CountDownLatch(2));

        // Add a listener to monitor the speed test progress
        speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {
            @Override
            public void onCompletion(SpeedTestReport report) {
                BigDecimal speed = report.getTransferRateBit().divide(new BigDecimal(1_000_000), 2, RoundingMode.HALF_UP);
                System.out.println("Test completed!");
                System.out.println(report.getSpeedTestMode() + " speed: " + speed + " Mbps");
                latchRef.get().countDown();
            }

            @Override
            public void onError(SpeedTestError speedTestError, String errorMessage) {
                System.out.println("Error: " + errorMessage);
                latchRef.get().countDown();
            }

            @Override
            public void onProgress(float percent, SpeedTestReport report) {
                BigDecimal speed = report.getTransferRateBit().divide(new BigDecimal(1_000_000), 2, RoundingMode.HALF_UP);
                System.out.println("[" + report.getSpeedTestMode() + "] Progress: " + percent + "% - Speed: " + speed + " Mbps");
            }
        });

        // Start download test
        System.out.println("Starting download test...");
        speedTestSocket.startDownload("https://speed.cloudflare.com/__down?bytes=1000000");

        // Wait for download test to complete
        latchRef.get().await(30, TimeUnit.SECONDS);

        // Reset the latch for the upload test
        latchRef.set(new CountDownLatch(1));

        // Start upload test
        System.out.println("\nStarting upload test...");
        speedTestSocket.startUpload("https://speed.cloudflare.com/__up", 1000000); // Upload 1MB of data

        // Wait for upload test to complete
        latchRef.get().await(30, TimeUnit.SECONDS);

        System.out.println("All tests completed.");
    }
}