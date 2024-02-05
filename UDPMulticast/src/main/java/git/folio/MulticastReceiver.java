package git.folio;

import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;

public class MulticastReceiver {
    private static final String MULTICAST_ADDRESS = "230.0.0.1";
    private static final int PORT = 4446;

    public static void main(String[] args) {
        MulticastSocket socket = null;
        try {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket = new MulticastSocket(PORT);
            socket.joinGroup(group);

            System.out.println("Multicast Receiver running...");

            while (true) {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                // Decode received bytes
                ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
                DataInputStream dis = new DataInputStream(bais);

                int intValue = dis.readInt();
                float floatValue = dis.readFloat();
                String stringValue = dis.readUTF();

                System.out.println("Received multicast message:");
                System.out.println("Int value: " + intValue);
                System.out.println("Float value: " + floatValue);
                System.out.println("String value: " + stringValue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}
