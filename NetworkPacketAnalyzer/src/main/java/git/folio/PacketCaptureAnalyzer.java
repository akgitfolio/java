package git.folio;

import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.NifSelector;

import java.io.IOException;

public class PacketCaptureAnalyzer {

    private static final int SNAPSHOT_LENGTH = 65536; // in bytes
    private static final int READ_TIMEOUT = 50; // in milliseconds

    public static void main(String[] args) throws PcapNativeException, NotOpenException {
        // Select network interface
        PcapNetworkInterface device = getNetworkDevice();
        if (device == null) {
            System.out.println("No network interface selected. Exiting.");
            return;
        }

        System.out.println("You chose: " + device);

        // Open the device and get a handle
        final PcapHandle handle = device.openLive(SNAPSHOT_LENGTH, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);

        // Set a filter to capture only TCP packets on port 80 (HTTP)
        String filter = "tcp port 80";
//        handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);

        // Create a listener that defines how to handle the received packets
        PacketListener listener = new PacketListener() {
            @Override
            public void gotPacket(Packet packet) {
                // Print packet information
                System.out.println(packet);
            }
        };

        // Tell the handle to loop using the listener we created
        try {
            int maxPackets = -1; // -1 means loop indefinitely
            handle.loop(maxPackets, listener);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Close the handle after we're done
        handle.close();
    }

    private static PcapNetworkInterface getNetworkDevice() {
        PcapNetworkInterface device = null;
        try {
            device = new NifSelector().selectNetworkInterface();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return device;
    }
}