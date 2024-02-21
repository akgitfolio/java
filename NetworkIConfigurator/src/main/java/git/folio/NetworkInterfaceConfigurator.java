package git.folio;

import java.net.*;
import java.util.*;

public class NetworkInterfaceConfigurator {

    public static Map<String, Map<String, String>> getAllNetworkInterfaces() throws SocketException {
        Map<String, Map<String, String>> interfacesMap = new HashMap<>();

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            Map<String, String> interfaceInfo = new HashMap<>();
            interfaceInfo.put("Display Name", networkInterface.getDisplayName());
            interfaceInfo.put("Name", networkInterface.getName());
            interfaceInfo.put("Is Up", String.valueOf(networkInterface.isUp()));
            interfaceInfo.put("Is Loopback", String.valueOf(networkInterface.isLoopback()));
            interfaceInfo.put("Is Virtual", String.valueOf(networkInterface.isVirtual()));
            interfaceInfo.put("Supports Multicast", String.valueOf(networkInterface.supportsMulticast()));
            interfaceInfo.put("MTU", String.valueOf(networkInterface.getMTU()));

            List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
            for (int i = 0; i < interfaceAddresses.size(); i++) {
                InterfaceAddress address = interfaceAddresses.get(i);
                interfaceInfo.put("IP Address " + (i + 1), address.getAddress().getHostAddress());
                interfaceInfo.put("Network Prefix Length " + (i + 1), String.valueOf(address.getNetworkPrefixLength()));
                if (address.getBroadcast() != null) {
                    interfaceInfo.put("Broadcast " + (i + 1), address.getBroadcast().getHostAddress());
                }
            }

            byte[] mac = networkInterface.getHardwareAddress();
            if (mac != null) {
                StringBuilder macAddress = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    macAddress.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }
                interfaceInfo.put("MAC Address", macAddress.toString());
            }

            interfacesMap.put(networkInterface.getName(), interfaceInfo);
        }

        return interfacesMap;
    }

    public static void main(String[] args) {
        try {
            Map<String, Map<String, String>> networkInterfaces = getAllNetworkInterfaces();

            for (Map.Entry<String, Map<String, String>> entry : networkInterfaces.entrySet()) {
                System.out.println("Interface: " + entry.getKey());
                for (Map.Entry<String, String> info : entry.getValue().entrySet()) {
                    System.out.println("  " + info.getKey() + ": " + info.getValue());
                }
                System.out.println();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
