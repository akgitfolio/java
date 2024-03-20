package git.folio;

import java.io.*;
import java.util.*;

public class INIParser {
    private Map<String, Map<String, String>> sections;

    public INIParser() {
        sections = new LinkedHashMap<>();
    }

    public void parse(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentSection = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith(";") || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1);
                    sections.put(currentSection, new LinkedHashMap<>());
                } else if (currentSection != null) {
                    int separatorIndex = line.indexOf('=');
                    if (separatorIndex != -1) {
                        String key = line.substring(0, separatorIndex).trim();
                        String value = line.substring(separatorIndex + 1).trim();
                        sections.get(currentSection).put(key, value);
                    }
                }
            }
        }
    }

    public String getValue(String section, String key) {
        Map<String, String> sectionMap = sections.get(section);
        return sectionMap != null ? sectionMap.get(key) : null;
    }

    public Set<String> getSections() {
        return sections.keySet();
    }

    public Map<String, String> getSection(String section) {
        return sections.get(section);
    }

    public static void main(String[] args) {
        try {
            INIParser parser = new INIParser();
            parser.parse("sample.ini");

            // Example usage
            System.out.println("Sections: " + parser.getSections());
            System.out.println("Value of 'name' in 'owner' section: " + parser.getValue("owner", "name"));
            System.out.println("All values in 'database' section: " + parser.getSection("database"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}