package git.folio;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.logging.Logger;
import java.util.logging.Level;

public class FileSyncUtil {
    private static final Logger logger = Logger.getLogger(FileSyncUtil.class.getName());
    private final Path sourceDir;
    private final Path targetDir;
    private final boolean deleteExtraFiles;
    private long totalFiles;
    private long processedFiles;

    public FileSyncUtil(String sourcePath, String targetPath, boolean deleteExtraFiles) {
        this.sourceDir = Paths.get(sourcePath);
        this.targetDir = Paths.get(targetPath);
        this.deleteExtraFiles = deleteExtraFiles;
        this.totalFiles = 0;
        this.processedFiles = 0;
    }

    public void synchronize() throws IOException {
        logger.info("Starting synchronization from " + sourceDir + " to " + targetDir);

        // Count total files for progress tracking
        totalFiles = Files.walk(sourceDir).filter(Files::isRegularFile).count();

        // Synchronize directories
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetPath = targetDir.resolve(sourceDir.relativize(dir));
                if (!Files.exists(targetPath)) {
                    Files.createDirectory(targetPath);
                    logger.info("Created directory: " + targetPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                syncFile(file);
                return FileVisitResult.CONTINUE;
            }
        });

        // Delete extra files in target if option is enabled
        if (deleteExtraFiles) {
            deleteExtraFiles();
        }

        logger.info("Synchronization completed.");
    }

    private void syncFile(Path sourceFile) throws IOException {
        Path targetFile = targetDir.resolve(sourceDir.relativize(sourceFile));

        if (!Files.exists(targetFile) || !areFilesEqual(sourceFile, targetFile)) {
            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Copied: " + sourceFile + " to " + targetFile);
        }

        updateProgress();
    }

    private boolean areFilesEqual(Path file1, Path file2) throws IOException {
        if (Files.size(file1) != Files.size(file2)) {
            return false;
        }

        byte[] hash1 = calculateFileHash(file1);
        byte[] hash2 = calculateFileHash(file2);

        return MessageDigest.isEqual(hash1, hash2);
    }

    private byte[] calculateFileHash(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            return digest.digest();
        } catch (Exception e) {
            throw new IOException("Failed to calculate file hash", e);
        }
    }

    private void deleteExtraFiles() throws IOException {
        Files.walkFileTree(targetDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = targetDir.relativize(file);
                Path sourceFile = sourceDir.resolve(relativePath);
                if (!Files.exists(sourceFile)) {
                    Files.delete(file);
                    logger.info("Deleted extra file: " + file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Path relativePath = targetDir.relativize(dir);
                Path sourceDir = FileSyncUtil.this.sourceDir.resolve(relativePath);
                if (!Files.exists(sourceDir)) {
                    Files.delete(dir);
                    logger.info("Deleted extra directory: " + dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void updateProgress() {
        processedFiles++;
        double progress = (double) processedFiles / totalFiles * 100;
        logger.info(String.format("Progress: %.2f%% (%d/%d files)", progress, processedFiles, totalFiles));
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java FileSyncUtil <source_dir> <target_dir> [delete_extra_files]");
            return;
        }

        String sourceDir = args[0];
        String targetDir = args[1];
        boolean deleteExtraFiles = args.length > 2 && Boolean.parseBoolean(args[2]);

        FileSyncUtil syncUtility = new FileSyncUtil(sourceDir, targetDir, deleteExtraFiles);

        try {
            syncUtility.synchronize();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Synchronization failed", e);
        }
    }
}