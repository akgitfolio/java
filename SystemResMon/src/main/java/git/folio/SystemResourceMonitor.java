package git.folio;

import java.lang.management.*;
import java.io.File;
import java.text.DecimalFormat;

public class SystemResourceMonitor {

    private static final DecimalFormat df = new DecimalFormat("0.00");

    public static void main(String[] args) {
        while (true) {
            printCPUUsage();
            printMemoryUsage();
            printDiskUsage();
            System.out.println("--------------------");

            try {
                Thread.sleep(5000); // Wait for 5 seconds before next update
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void printCPUUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            double cpuUsage = sunOsBean.getSystemCpuLoad() * 100;
            System.out.println("CPU Usage: " + df.format(cpuUsage) + "%");
        } else {
            System.out.println("CPU Usage: Not available");
        }
    }

    private static void printMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        System.out.println("Memory Usage:");
        System.out.println("  Total: " + formatSize(totalMemory));
        System.out.println("  Used:  " + formatSize(usedMemory));
        System.out.println("  Free:  " + formatSize(freeMemory));
    }

    private static void printDiskUsage() {
        File[] roots = File.listRoots();
        for (File root : roots) {
            long totalSpace = root.getTotalSpace();
            long usableSpace = root.getUsableSpace();
            long usedSpace = totalSpace - usableSpace;

            System.out.println("Disk Usage for " + root.getAbsolutePath());
            System.out.println("  Total: " + formatSize(totalSpace));
            System.out.println("  Used:  " + formatSize(usedSpace));
            System.out.println("  Free:  " + formatSize(usableSpace));
        }
    }

    private static String formatSize(long size) {
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double sizeInUnit = size;

        while (sizeInUnit >= 1024 && unitIndex < units.length - 1) {
            sizeInUnit /= 1024;
            unitIndex++;
        }

        return df.format(sizeInUnit) + " " + units[unitIndex];
    }
}