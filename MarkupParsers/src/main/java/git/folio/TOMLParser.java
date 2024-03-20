package git.folio;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class TOMLParser {
    private Map<String, Object> data;
    private BufferedReader reader;

    public TOMLParser() {
        this.data = new LinkedHashMap<>();
    }

    public void parseFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            this.reader = reader;
            String line;
            Map<String, Object> currentTable = data;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }
                if (line.startsWith("[")) {
                    currentTable = handleTable(line);
                } else {
                    handleKeyValue(line, currentTable);
                }
            }
        }
    }

    private Map<String, Object> handleTable(String line) {
        String tableName = line.substring(1, line.length() - 1).trim();
        String[] parts = tableName.split("\\.");
        Map<String, Object> current = data;
        for (String part : parts) {
            current = (Map<String, Object>) current.computeIfAbsent(part, k -> new LinkedHashMap<>());
        }
        return current;
    }

    private void handleKeyValue(String line, Map<String, Object> currentTable) throws IOException {
        int equalsIndex = line.indexOf('=');
        if (equalsIndex == -1) {
            throw new IllegalArgumentException("Invalid key-value pair: " + line);
        }
        String key = line.substring(0, equalsIndex).trim();
        String value = line.substring(equalsIndex + 1).trim();

        // Handle multi-line arrays and strings
        if ((value.startsWith("[") && !value.endsWith("]")) ||
                (value.startsWith("\"") && !value.endsWith("\""))) {
            StringBuilder fullValue = new StringBuilder(value);
            String nextLine;
            while ((nextLine = reader.readLine()) != null) {
                fullValue.append("\n").append(nextLine.trim());
                if ((value.startsWith("[") && nextLine.trim().endsWith("]")) ||
                        (value.startsWith("\"") && nextLine.trim().endsWith("\""))) {
                    break;
                }
            }
            value = fullValue.toString();
        }

        currentTable.put(key, parseValue(value));
    }

    private Object parseValue(String value) {
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        } else if (value.equals("true") || value.equals("false")) {
            return Boolean.parseBoolean(value);
        } else if (value.matches("-?\\d+")) {
            return Long.parseLong(value);
        } else if (value.matches("-?\\d+\\.\\d+")) {
            return Double.parseDouble(value);
        } else if (value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(Z|[+-]\\d{2}:\\d{2})")) {
            return OffsetDateTime.parse(value);
        } else if (value.startsWith("[") && value.endsWith("]")) {
            return parseArray(value);
        } else if (value.startsWith("{") && value.endsWith("}")) {
            return parseInlineTable(value);
        } else {
            throw new IllegalArgumentException("Unsupported value type: " + value);
        }
    }

    private List<Object> parseArray(String value) {
        List<Object> array = new ArrayList<>();
        value = value.substring(1, value.length() - 1).trim();

        StringBuilder element = new StringBuilder();
        int nestedBrackets = 0;
        boolean inQuotes = false;

        for (char c : value.toCharArray()) {
            if (c == '"' && (element.length() == 0 || element.charAt(element.length() - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            if (!inQuotes) {
                if (c == '[') nestedBrackets++;
                if (c == ']') nestedBrackets--;
            }

            if (c == ',' && nestedBrackets == 0 && !inQuotes) {
                array.add(parseValue(element.toString().trim()));
                element = new StringBuilder();
            } else {
                element.append(c);
            }
        }

        if (element.length() > 0) {
            array.add(parseValue(element.toString().trim()));
        }

        return array;
    }

    private Map<String, Object> parseInlineTable(String value) {
        Map<String, Object> table = new LinkedHashMap<>();
        value = value.substring(1, value.length() - 1).trim();

        StringBuilder pair = new StringBuilder();
        boolean inQuotes = false;

        for (char c : value.toCharArray()) {
            if (c == '"' && (pair.length() == 0 || pair.charAt(pair.length() - 1) != '\\')) {
                inQuotes = !inQuotes;
            }

            if (c == ',' && !inQuotes) {
                addPairToTable(table, pair.toString());
                pair = new StringBuilder();
            } else {
                pair.append(c);
            }
        }

        if (pair.length() > 0) {
            addPairToTable(table, pair.toString());
        }

        return table;
    }

    private void addPairToTable(Map<String, Object> table, String pair) {
        String[] keyValue = pair.split("=", 2);
        if (keyValue.length == 2) {
            String key = keyValue[0].trim();
            Object parsedValue = parseValue(keyValue[1].trim());
            table.put(key, parsedValue);
        }
    }

    public Object get(String key) {
        String[] parts = key.split("\\.");
        Map<String, Object> current = data;
        for (int i = 0; i < parts.length - 1; i++) {
            current = (Map<String, Object>) current.get(parts[i]);
            if (current == null) {
                return null;
            }
        }
        return current.get(parts[parts.length - 1]);
    }

    public String getString(String key) {
        Object value = get(key);
        return (value instanceof String) ? (String) value : null;
    }

    public Long getLong(String key) {
        Object value = get(key);
        return (value instanceof Long) ? (Long) value : null;
    }

    public Double getDouble(String key) {
        Object value = get(key);
        return (value instanceof Double) ? (Double) value : null;
    }

    public Boolean getBoolean(String key) {
        Object value = get(key);
        return (value instanceof Boolean) ? (Boolean) value : null;
    }

    public OffsetDateTime getDateTime(String key) {
        Object value = get(key);
        return (value instanceof OffsetDateTime) ? (OffsetDateTime) value : null;
    }

    public List<Object> getList(String key) {
        Object value = get(key);
        return (value instanceof List) ? (List<Object>) value : null;
    }

    public Map<String, Object> getTable(String key) {
        Object value = get(key);
        return (value instanceof Map) ? (Map<String, Object>) value : null;
    }

    public static void main(String[] args) {
        try {
            TOMLParser parser = new TOMLParser();
            parser.parseFile("sample.toml");

            // Example usage
            String title = parser.getString("title");
            System.out.println("Title: " + title);

            String ownerName = parser.getString("owner.name");
            System.out.println("Owner Name: " + ownerName);

            OffsetDateTime ownerDob = parser.getDateTime("owner.dob");
            System.out.println("Owner DOB: " + ownerDob);

            Boolean dbEnabled = parser.getBoolean("database.enabled");
            System.out.println("Database Enabled: " + dbEnabled);

            List<Object> ports = parser.getList("database.ports");
            System.out.println("Database Ports: " + ports);

            List<Object> data = parser.getList("database.data");
            System.out.println("Database Data: " + data);

            Map<String, Object> tempTargets = parser.getTable("database.temp_targets");
            System.out.println("Temperature Targets: " + tempTargets);

            Map<String, Object> serverAlpha = parser.getTable("servers.alpha");
            String alphaIP = (String) serverAlpha.get("ip");
            String alphaRole = (String) serverAlpha.get("role");
            System.out.println("Server Alpha IP: " + alphaIP + ", Role: " + alphaRole);

            Map<String, Object> serverBeta = parser.getTable("servers.beta");
            String betaIP = (String) serverBeta.get("ip");
            String betaRole = (String) serverBeta.get("role");
            System.out.println("Server Beta IP: " + betaIP + ", Role: " + betaRole);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}