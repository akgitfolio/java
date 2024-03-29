package git.folio;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProducerPoll {
    public static void main(String[] args) {
        try {
            MessageQueue queue = new MessageQueue();
            System.out.println("Producer started. Sending current date and time continuously.");

            while (true) {
                // Get and send current date and time
                String currentDateTime = getCurrentDateTime();
                queue.send(currentDateTime);
                System.out.println("Sent: " + currentDateTime);

                // Simulate polling for events from an external system
                String externalEvent = pollExternalSystem();
                if (externalEvent != null) {
                    queue.send(externalEvent);
                    System.out.println("Sent external event: " + externalEvent);
                }

                Thread.sleep(1000); // Wait for 1 second before next iteration
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    private static String pollExternalSystem() {
        // Simulate polling an external system for events
        if (Math.random() < 0.2) { // 20% chance of generating an event
            return "External Event " + System.currentTimeMillis();
        }
        return null;
    }
}