package git.folio;

import java.io.*;
import java.net.*;
import java.util.*;

public class SimpleIntrusionDetectionSystem {

    private static final String HOST = "0.0.0.0"; // accept all to make test work
    private static final int PORT = 3000;
    private static final int MAX_CONNECTIONS = 5;
    private static final int CONNECTION_TIMEOUT = 5000; // 5 seconds
    private static final Set<String> blacklist = new HashSet<>(Arrays.asList("192.168.1.100", "10.0.0.1", "127.0.0.1" )); // test intrusion from localhost for demo

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(HOST))) {
            System.out.println("Intrusion Detection System started on " + HOST + ":" + PORT);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new ConnectionHandler(clientSocket)).start();
                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on " + HOST + ":" + PORT);
            System.exit(-1);
        }
    }

    private static class ConnectionHandler implements Runnable {
        private final Socket clientSocket;

        public ConnectionHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                System.out.println("New connection from: " + clientAddress);

                if (blacklist.contains(clientAddress)) {
                    System.out.println("Blocked connection from blacklisted IP: " + clientAddress);
                    clientSocket.close();
                    return;
                }

                if (Thread.activeCount() > MAX_CONNECTIONS) {
                    System.out.println("Too many connections. Rejecting: " + clientAddress);
                    clientSocket.close();
                    return;
                }

                clientSocket.setSoTimeout(CONNECTION_TIMEOUT);

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine = in.readLine();

                if (inputLine != null && inputLine.toLowerCase().contains("attack")) {
                    System.out.println("Potential attack detected from " + clientAddress + ": " + inputLine);
                } else {
                    System.out.println("Normal activity from " + clientAddress + ": " + inputLine);
                }

                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error handling client connection: " + e.getMessage());
            }
        }
    }
}