package git.folio;

import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import org.pcap4j.util.NifSelector;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PacketSniffer {
    private static final String READ_TIMEOUT_KEY = PacketSniffer.class.getName() + ".readTimeout";
    private static final int READ_TIMEOUT = Integer.getInteger(READ_TIMEOUT_KEY, 10); // [ms]

    private static final String SNAPLEN_KEY = PacketSniffer.class.getName() + ".snaplen";
    private static final int SNAPLEN = Integer.getInteger(SNAPLEN_KEY, 65536); // [bytes]

    private static volatile boolean running = true;

    public static void main(String[] args) throws PcapNativeException, NotOpenException {
        PcapNetworkInterface nif;
        try {
            nif = new NifSelector().selectNetworkInterface();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (nif == null) {
            System.out.println("No interface selected. Exiting.");
            return;
        }

        System.out.println("Selected interface: " + nif.getName());

        final PcapHandle handle = nif.openLive(SNAPLEN, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);

        // Set a filter if needed
        // handle.setFilter("tcp port 80", BpfProgram.BpfCompileMode.OPTIMIZE);

        PacketListener listener = new PacketListener() {
            @Override
            public void gotPacket(Packet packet) {
                System.out.println("Captured packet:");
                System.out.println(packet);

                // Packet parsing and analysis
                if (packet.contains(IpV4Packet.class)) {
                    IpV4Packet ipv4Packet = packet.get(IpV4Packet.class);
                    System.out.println("Source IP: " + ipv4Packet.getHeader().getSrcAddr());
                    System.out.println("Destination IP: " + ipv4Packet.getHeader().getDstAddr());
                }

                if (packet.contains(TcpPacket.class)) {
                    TcpPacket tcpPacket = packet.get(TcpPacket.class);
                    System.out.println("Source Port: " + tcpPacket.getHeader().getSrcPort());
                    System.out.println("Destination Port: " + tcpPacket.getHeader().getDstPort());
                }

                System.out.println();
            }
        };

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                handle.loop(-1, listener); // -1 means loop indefinitely
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (PcapNativeException e) {
                e.printStackTrace();
            } catch (NotOpenException e) {
                e.printStackTrace();
            }
        });

        System.out.println("Packet capture started. Press Enter to stop.");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        running = false;
        handle.breakLoop();
        handle.close();
        executor.shutdown();
        System.out.println("Packet capture stopped.");
    }
}