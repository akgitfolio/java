package git.folio;

import java.io.*;
import java.util.*;

public class SimpleDiffTool {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java SimpleDiffTool <file1> <file2>");
            return;
        }

        String file1 = args[0];
        String file2 = args[1];

        try {
            List<String> lines1 = readFile(file1);
            List<String> lines2 = readFile(file2);

            List<String[]> diff = generateDiff(lines1, lines2);

            for (String[] lineDiff : diff) {
                System.out.println("Line " + lineDiff[0] + ":");
                System.out.println("< " + lineDiff[1]);
                System.out.println("> " + lineDiff[2]);
                System.out.println("---");
            }
        } catch (IOException e) {
            System.out.println("Error reading files: " + e.getMessage());
        }
    }

    private static List<String> readFile(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static List<String[]> generateDiff(List<String> lines1, List<String> lines2) {
        List<String[]> diff = new ArrayList<>();
        int maxLines = Math.max(lines1.size(), lines2.size());

        for (int i = 0; i < maxLines; i++) {
            String line1 = i < lines1.size() ? lines1.get(i) : "";
            String line2 = i < lines2.size() ? lines2.get(i) : "";

            if (!line1.equals(line2)) {
                String[] fragments = extractDifference(line1, line2);
                diff.add(new String[]{String.valueOf(i + 1), fragments[0], fragments[1]});
            }
        }

        return diff;
    }

    private static String[] extractDifference(String line1, String line2) {
        StringBuilder diff1 = new StringBuilder();
        StringBuilder diff2 = new StringBuilder();
        int i = 0, j = 0;

        while (i < line1.length() || j < line2.length()) {
            if (i < line1.length() && j < line2.length() && line1.charAt(i) == line2.charAt(j)) {
                i++;
                j++;
            } else {
                while (i < line1.length() && (j >= line2.length() || line1.charAt(i) != line2.charAt(j))) {
                    diff1.append(line1.charAt(i++));
                }
                while (j < line2.length() && (i >= line1.length() || line1.charAt(i) != line2.charAt(j))) {
                    diff2.append(line2.charAt(j++));
                }
            }
        }

        return new String[]{diff1.toString(), diff2.toString()};
    }
}
