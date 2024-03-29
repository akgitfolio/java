package git.folio;

import java.io.IOException;
import java.util.Scanner;

public class Producer {
    public static void main(String[] args) {
        try {
            MessageQueue queue = new MessageQueue();
            Scanner scanner = new Scanner(System.in);
            System.out.println("Producer started. Enter messages to send (type 'exit' to quit):");

            while (true) {
                System.out.print("Enter message: ");
                String input = scanner.nextLine().trim();

                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }

                if (!input.isEmpty()) {
                    queue.send(input);
                    System.out.println("Sent: " + input);
                }

                // Simulate polling for events from an external system
                String externalEvent = pollExternalSystem();
                if (externalEvent != null) {
                    queue.send(externalEvent);
                    System.out.println("Sent external event: " + externalEvent);
                }

                Thread.sleep(1000); // Wait for 1 second before next poll
            }

            queue.close();
            scanner.close();
            System.out.println("Producer terminated.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String pollExternalSystem() {
        // Simulate polling an external system for events
        // In a real scenario, this method would interact with an actual external system
        if (Math.random() < 0.2) { // 20% chance of generating an event
            return "External Event " + System.currentTimeMillis();
        }
        return null;
    }
}