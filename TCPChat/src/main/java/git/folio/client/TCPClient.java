package git.folio.client;

import java.io.*;
import java.net.Socket;

public class TCPClient {
    private String hostname;
    private int port;

    public TCPClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void connect() {
        try (
                Socket socket = new Socket(hostname, port);
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input))
        ) {
            System.out.println("Connected to server");

            String message = "Hello from client";
            writer.println(message);

            String response = reader.readLine();
            System.out.println("Server response: " + response);

        } catch (IOException ex) {
            System.out.println("Client exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TCPClient client = new TCPClient("localhost", 5000);
        client.connect();
    }
}