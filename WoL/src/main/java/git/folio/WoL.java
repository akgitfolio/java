package git.folio;

import java.net.*;
import java.util.regex.*;

public class WoL {

    private static final int PORT = 9; // Standard WoL port
    private static final String MAC_REGEX = "([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})";

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java WoL <broadcast-ip> <mac-address>");
            System.out.println("Example: java WoL 192.168.0.255 00:0D:61:08:22:4A");
            System.exit(1);
        }

        String broadcastIP = args[0];
        String macAddress = args[1];

        try {
            wOnL(broadcastIP, macAddress);
            System.out.println("W-o-L packet sent successfully.");
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void wOnL(String broadcastIP, String macAddress) throws Exception {
        // Validate MAC address format
        if (!isValidMacAddress(macAddress)) {
            throw new IllegalArgumentException("Invalid MAC address format");
        }

        byte[] macBytes = getMacBytes(macAddress);
        byte[] bytes = new byte[6 + 16 * macBytes.length];

        // Fill first 6 bytes with 0xFF
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xFF;
        }

        // Fill remaining bytes with MAC address repeated 16 times
        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        // Create and send UDP packet
        InetAddress address = InetAddress.getByName(broadcastIP);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(packet);
        }
    }

    private static byte[] getMacBytes(String macAddress) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macAddress.split("(:|-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address");
        }
        return bytes;
    }

    private static boolean isValidMacAddress(String macAddress) {
        Pattern pattern = Pattern.compile(MAC_REGEX);
        return pattern.matcher(macAddress).matches();
    }
}
