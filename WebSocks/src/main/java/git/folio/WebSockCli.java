package git.folio;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.Scanner;

public class WebSockCli extends WebSocketClient {

    private boolean running = true;

    public WebSockCli(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to server");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed");
        running = false;
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        running = false;
    }

    public static void main(String[] args) throws Exception {
        WebSockCli client = new WebSockCli(new URI("ws://localhost:8887"));
        client.connectBlocking();

        // Start a separate thread for sending ping messages
        Thread pingThread = new Thread(() -> {
            while (client.running) {
                try {
                    client.sendPing();
                    Thread.sleep(30000); // Send ping every 30 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        pingThread.start();

        // Read input from CLI and send messages
        Scanner scanner = new Scanner(System.in);
        while (client.running) {
            System.out.print("Enter message (or 'exit' to quit): ");
            String input = scanner.nextLine();
            if ("exit".equalsIgnoreCase(input)) {
                client.running = false;
            } else {
                client.send(input);
            }
        }

        client.close();
        scanner.close();
    }
}