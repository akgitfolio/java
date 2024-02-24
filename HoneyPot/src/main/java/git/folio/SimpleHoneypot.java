package git.folio;
import java.io.*;
import java.net.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class SimpleHoneypot {
    private static final int PORT = 3000; // Choose any port you want to monitor
    private static final String LOG_FILE = "honeypot.log";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Honeypot listening on port " + PORT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    logConnection(clientAddress);

                    // Send a fake response
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: text/html");
                    out.println("\r\n");
                    out.println("<html><body><h1>Welcome to the server</h1></body></html>");
                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + PORT);
            System.exit(-1);
        }
    }

    private static void logConnection(String clientAddress) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());
        String logEntry = timestamp + " - Connection attempt from: " + clientAddress;

        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(logEntry);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }

        System.out.println(logEntry);
    }
}
