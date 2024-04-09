package git.folio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class FileIntegrityChecker {

    private static final String BASELINE_FILE = "baseline.dat";

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java FileIntegrityChecker <mode> <directory>");
            System.out.println("Modes: baseline, check");
            return;
        }

        String mode = args[0];
        String directory = args[1];

        FileIntegrityChecker checker = new FileIntegrityChecker();

        if ("baseline".equals(mode)) {
            checker.createBaseline(directory);
        } else if ("check".equals(mode)) {
            checker.checkIntegrity(directory);
        } else {
            System.out.println("Invalid mode. Use 'baseline' or 'check'.");
        }
    }

    public void createBaseline(String directory) {
        Map<String, String> fileHashes = new HashMap<>();

        try {
            Files.walk(Paths.get(directory))
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            String hash = calculateFileHash(file.toFile());
                            fileHashes.put(file.toString(), hash);
                        } catch (Exception e) {
                            System.out.println("Error processing file: " + file);
                            e.printStackTrace();
                        }
                    });

            saveBaseline(fileHashes);
            System.out.println("Baseline created successfully.");
        } catch (IOException e) {
            System.out.println("Error creating baseline:");
            e.printStackTrace();
        }
    }

    public void checkIntegrity(String directory) {
        Map<String, String> baselineHashes = loadBaseline();
        if (baselineHashes == null) {
            System.out.println("No baseline found. Please create a baseline first.");
            return;
        }

        try {
            Files.walk(Paths.get(directory))
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            String currentHash = calculateFileHash(file.toFile());
                            String baselineHash = baselineHashes.get(file.toString());

                            if (baselineHash == null) {
                                System.out.println("New file detected: " + file);
                            } else if (!currentHash.equals(baselineHash)) {
                                System.out.println("File modified: " + file);
                            }

                            baselineHashes.remove(file.toString());
                        } catch (Exception e) {
                            System.out.println("Error processing file: " + file);
                            e.printStackTrace();
                        }
                    });

            // Check for deleted files
            for (String remainingFile : baselineHashes.keySet()) {
                System.out.println("File deleted: " + remainingFile);
            }

            System.out.println("Integrity check completed.");
        } catch (IOException e) {
            System.out.println("Error checking integrity:");
            e.printStackTrace();
        }
    }

    private String calculateFileHash(File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];
        int bytesCount;

        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        fis.close();

        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private void saveBaseline(Map<String, String> fileHashes) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BASELINE_FILE))) {
            oos.writeObject(fileHashes);
        } catch (IOException e) {
            System.out.println("Error saving baseline:");
            e.printStackTrace();
        }
    }

    private Map<String, String> loadBaseline() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BASELINE_FILE))) {
            return (Map<String, String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading baseline:");
            e.printStackTrace();
            return null;
        }
    }
}
