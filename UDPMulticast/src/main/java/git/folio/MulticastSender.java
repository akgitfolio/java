package git.folio;

import java.net.*;
import java.io.*;

public class MulticastSender {
    private static final String MULTICAST_ADDRESS = "230.0.0.1";
    private static final int PORT = 4446;

    public static void main(String[] args) {
        MulticastSocket socket = null;
        try {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket = new MulticastSocket();

            // Sample data to send
            int intValue = 42;
            float floatValue = 3.14f;
            String stringValue = "Hello, Multicast!";

            // Encode data to bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(intValue);
            dos.writeFloat(floatValue);
            dos.writeUTF(stringValue);
            byte[] data = baos.toByteArray();

            DatagramPacket packet = new DatagramPacket(data, data.length, group, PORT);
            socket.send(packet);

            System.out.println("Sent multicast message");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}
