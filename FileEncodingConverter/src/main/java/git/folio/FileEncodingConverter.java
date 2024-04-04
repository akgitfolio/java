package git.folio;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

public class FileEncodingConverter {

    public static void convertFile(String inputFile, String outputFile, String fromEncoding, String toEncoding)
            throws IOException, UnsupportedEncodingException {

        // Validate encodings
        if (!Charset.isSupported(fromEncoding) || !Charset.isSupported(toEncoding)) {
            throw new UnsupportedEncodingException("One or both of the specified encodings are not supported.");
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFile), fromEncoding));
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(new FileOutputStream(outputFile), toEncoding))) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public static List<String> getSupportedEncodings() {
        return new ArrayList<>(Charset.availableCharsets().keySet());
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("File Encoding Converter");
        System.out.println("----------------------");

        System.out.print("Enter input file path: ");
        String inputFile = scanner.nextLine();

        System.out.print("Enter output file path: ");
        String outputFile = scanner.nextLine();

        System.out.print("Enter source encoding: ");
        String fromEncoding = scanner.nextLine();

        System.out.print("Enter target encoding: ");
        String toEncoding = scanner.nextLine();

        try {
            convertFile(inputFile, outputFile, fromEncoding, toEncoding);
            System.out.println("File converted successfully!");
        } catch (UnsupportedEncodingException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Supported encodings: " + getSupportedEncodings());
        } catch (IOException e) {
            System.out.println("Error reading or writing file: " + e.getMessage());
        }

        scanner.close();
    }
}
