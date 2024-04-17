package git.folio;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WebsiteAvailabilityChecker {
    private static final int TIMEOUT = 5000; // 5 seconds

    public static void main(String[] args) {
        String url = "https://www.example.com"; // Replace with the website you want to check
        boolean isAvailable = checkWebsiteAvailability(url);
        System.out.println("Website " + url + " is " + (isAvailable ? "available" : "unavailable"));
    }

    public static boolean checkWebsiteAvailability(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return (200 <= responseCode && responseCode <= 399);
        } catch (MalformedURLException e) {
            System.err.println("Invalid URL: " + urlString);
            return false;
        } catch (IOException e) {
            System.err.println("Error checking website availability: " + e.getMessage());
            return false;
        }
    }
}