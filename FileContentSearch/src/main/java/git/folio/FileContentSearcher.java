package git.folio;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;

public class FileContentSearcher {

    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the directory path to search: ");
        String directoryPath = scanner.nextLine();

        System.out.print("Enter the search term: ");
        String searchTerm = scanner.nextLine();

        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            System.out.println("Invalid directory path.");
            return;
        }

        List<Path> matchingFiles = searchFiles(directory, searchTerm);

        if (matchingFiles.isEmpty()) {
            System.out.println("No files found containing the search term.");
        } else {
            System.out.println("Files containing the search term:");
            for (Path file : matchingFiles) {
                System.out.println(file.toAbsolutePath());
            }
        }

        scanner.close();
    }

    private static List<Path> searchFiles(Path directory, String searchTerm) {
        List<Path> result = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    executor.submit(() -> {
                        if (searchInFile(file, searchTerm)) {
                            result.add(file);
                        }
                    });
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.out.println("Error searching files: " + e.getMessage());
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result;
    }

    private static boolean searchInFile(Path file, String searchTerm) {
        try {
            String content = new String(Files.readAllBytes(file));
            return content.toLowerCase().contains(searchTerm.toLowerCase());
        } catch (IOException e) {
            System.out.println("Error reading file: " + file.toAbsolutePath());
            return false;
        }
    }
}