package git.folio;

import java.io.IOException;

public class Consumer {
    public static void main(String[] args) {
        try {
            MessageQueue queue = new MessageQueue();
            while (true) {
                String message = queue.receive();
                if (message != null) {
                    System.out.println("Received: " + message);
                } else {
                    System.out.println("No message available");
                    Thread.sleep(1000); // Wait before checking again
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}