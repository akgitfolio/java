package git.folio;

import java.io.*;
import java.util.zip.*;

public class FileCompressor {

    public static void compressFile(String sourceFile, String compressedFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(compressedFile);
             GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                gzipOS.write(buffer, 0, len);
            }
        }
        System.out.println("File compressed successfully.");
    }

    public static void decompressFile(String compressedFile, String decompressedFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(compressedFile);
             GZIPInputStream gzipIS = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(decompressedFile)) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIS.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        }
        System.out.println("File decompressed successfully.");
    }

    public static void main(String[] args) {
        String sourceFile = "sample.txt";
        String compressedFile = "sample.gz";
        String decompressedFile = "sample_decompressed.txt";

        try {
            // Compress the file
            compressFile(sourceFile, compressedFile);

            // Decompress the file
            decompressFile(compressedFile, decompressedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}