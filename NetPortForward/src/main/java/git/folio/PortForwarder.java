package git.folio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PortForwarder {
    private static final int BUFFER_SIZE = 8192;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PortForwarder <local_port> <remote_host> <remote_port>");
            System.exit(1);
        }

        int localPort = Integer.parseInt(args[0]);
        String remoteHost = args[1];
        int remotePort = Integer.parseInt(args[2]);

        try (ServerSocket serverSocket = new ServerSocket(localPort)) {
            System.out.println("Port forwarder listening on port " + localPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from " + clientSocket.getInetAddress());

                try {
                    Socket remoteSocket = new Socket(remoteHost, remotePort);
                    System.out.println("Connected to remote host: " + remoteHost + ":" + remotePort);

                    Thread clientToRemote = new ForwardThread(clientSocket, remoteSocket);
                    Thread remoteToClient = new ForwardThread(remoteSocket, clientSocket);

                    clientToRemote.start();
                    remoteToClient.start();
                } catch (IOException e) {
                    System.err.println("Error connecting to remote host: " + e.getMessage());
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    private static class ForwardThread extends Thread {
        private final Socket sourceSocket;
        private final Socket destinationSocket;

        public ForwardThread(Socket sourceSocket, Socket destinationSocket) {
            this.sourceSocket = sourceSocket;
            this.destinationSocket = destinationSocket;
        }

        @Override
        public void run() {
            try (InputStream input = sourceSocket.getInputStream();
                 OutputStream output = destinationSocket.getOutputStream()) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    output.flush();
                }
            } catch (IOException e) {
                System.err.println("Error in forwarding: " + e.getMessage());
            } finally {
                try {
                    sourceSocket.close();
                    destinationSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing sockets: " + e.getMessage());
                }
            }
        }
    }
}