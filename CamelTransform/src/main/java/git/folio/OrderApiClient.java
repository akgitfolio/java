package git.folio;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class OrderApiClient {

    private static final String API_URL = "http://localhost:8060/api/order";

    public static void main(String[] args) {
        String jsonPayload = """
                {
                  "id": 1,
                  "customerName": "John Doe",
                  "total": 1500.0
                }
                """;

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

        } catch (Exception e) {
            System.err.println("Error sending request: " + e.getMessage());
            e.printStackTrace();
        }
    }
}