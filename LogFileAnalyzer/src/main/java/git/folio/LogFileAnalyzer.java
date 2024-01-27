package git.folio;

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class LogFileAnalyzer {

    private List<LogEntry> logEntries = new ArrayList<>();
    private Pattern logPattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}) \\[(\\w+)\\] (.*)");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void analyzeLogFile(String filePath, String keyword) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line, keyword);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found - " + filePath);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private void parseLine(String line, String keyword) {
        Matcher matcher = logPattern.matcher(line);
        if (matcher.find()) {
            try {
                LocalDateTime timestamp = LocalDateTime.parse(matcher.group(1), dateFormatter);
                String level = matcher.group(2);
                String message = matcher.group(3);
                if (keyword == null || message.toLowerCase().contains(keyword.toLowerCase())) {
                    logEntries.add(new LogEntry(timestamp, level, message));
                }
            } catch (DateTimeParseException e) {
                System.err.println("Error parsing date: " + matcher.group(1));
            }
        } else {
            System.err.println("Warning: Unparseable log entry: " + line);
        }
    }

    public void printEntries() {
        logEntries.forEach(System.out::println);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java LogFileAnalyzer <logfile> [keyword]");
            return;
        }

        LogFileAnalyzer analyzer = new LogFileAnalyzer();
        try {
            String keyword = args.length > 1 ? args[1] : null;
            analyzer.analyzeLogFile(args[0], keyword);
            analyzer.printEntries();
        } catch (IOException e) {
            System.err.println("Error analyzing log file: " + e.getMessage());
        }
    }
}

class LogEntry {
    private LocalDateTime timestamp;
    private String level;
    private String message;

    public LogEntry(LocalDateTime timestamp, String level, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
    }

    @Override
    public String toString() {
        return timestamp + " [" + level + "] " + message;
    }
}