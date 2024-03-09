package git.folio;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class NetworkSpeedTest {

    private static final String DOWNLOAD_URL = "https://speed.cloudflare.com/__down?bytes=25000000";
    private static final String UPLOAD_URL = "https://speed.cloudflare.com/__up";
    private static final int UPLOAD_SIZE = 1000000; // 1 MB

    public static void main(String[] args) {
        testDownloadSpeed();
        testUploadSpeed();
    }

    public static void testDownloadSpeed() {
        try {
            URL url = new URL(DOWNLOAD_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            long startTime = System.currentTimeMillis();

            InputStream in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytesRead = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
            }
            in.close();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            double speedMbps = (totalBytesRead * 8.0 / (1024 * 1024)) / (duration / 1000.0);
            System.out.printf("Download speed: %.2f Mbps%n", speedMbps);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testUploadSpeed() {
        try {
            URL url = new URL(UPLOAD_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            byte[] data = new byte[UPLOAD_SIZE];
            Arrays.fill(data, (byte) 'A');

            long startTime = System.currentTimeMillis();

            OutputStream out = conn.getOutputStream();
            out.write(data);
            out.flush();
            out.close();

            InputStream in = conn.getInputStream();
            in.close();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            double speedMbps = (UPLOAD_SIZE * 8.0 / (1024 * 1024)) / (duration / 1000.0);
            System.out.printf("Upload speed: %.2f Mbps%n", speedMbps);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}