package git.folio;

import java.io.*;
import java.util.*;

public class JSONParser {
    private String input;
    private int position;

    public JSONParser(String input) {
        this.input = input;
        this.position = 0;
    }

    public Object parse() {
        skipWhitespace();
        validateJson();
        char c = peek();
        if (c == '{') {
            return parseObject();
        } else if (c == '[') {
            return parseArray();
        } else if (c == '"') {
            return parseString();
        } else if (Character.isDigit(c) || c == '-') {
            return parseNumber();
        } else if (c == 't' || c == 'f') {
            return parseBoolean();
        } else if (c == 'n') {
            return parseNull();
        }
        throw new RuntimeException("Unexpected character: " + c + " at position " + position);
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new HashMap<>();
        consume('{');
        skipWhitespace();
        while (peek() != '}') {
            String key = parseString();
            skipWhitespace();
            consume(':');
            skipWhitespace();
            Object value = parse();
            map.put(key, value);
            skipWhitespace();
            if (peek() == ',') {
                consume(',');
                skipWhitespace();
            }
        }
        consume('}');
        return map;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        consume('[');
        skipWhitespace();
        while (peek() != ']') {
            list.add(parse());
            skipWhitespace();
            if (peek() == ',') {
                consume(',');
                skipWhitespace();
            }
        }
        consume(']');
        return list;
    }

    private String parseString() {
        StringBuilder sb = new StringBuilder();
        consume('"');
        while (peek() != '"') {
            char c = consume();
            if (c == '\\') {
                c = consume();
                switch (c) {
                    case '"':
                    case '\\':
                    case '/':
                        sb.append(c);
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    default:
                        throw new RuntimeException("Invalid escape character: \\" + c);
                }
            } else {
                sb.append(c);
            }
        }
        consume('"');
        return sb.toString();
    }

    private Number parseNumber() {
        StringBuilder sb = new StringBuilder();
        while (Character.isDigit(peek()) || peek() == '.' || peek() == '-' || peek() == 'e' || peek() == 'E') {
            sb.append(consume());
        }
        String numStr = sb.toString();
        try {
            return numStr.contains(".") || numStr.contains("e") || numStr.contains("E")
                    ? Double.parseDouble(numStr)
                    : Long.parseLong(numStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number format: " + numStr);
        }
    }

    private Boolean parseBoolean() {
        if (peek() == 't') {
            consume("true");
            return true;
        } else {
            consume("false");
            return false;
        }
    }

    private Object parseNull() {
        consume("null");
        return null;
    }

    private char peek() {
        if (position >= input.length()) {
            throw new RuntimeException("Unexpected end of input");
        }
        return input.charAt(position);
    }

    private char consume() {
        return input.charAt(position++);
    }

    private void consume(char c) {
        if (peek() != c) {
            throw new RuntimeException("Expected '" + c + "' but found '" + peek() + "' at position " + position);
        }
        position++;
    }

    private void consume(String s) {
        for (char c : s.toCharArray()) {
            consume(c);
        }
    }

    private void skipWhitespace() {
        while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
            position++;
        }
    }

    private void validateJson() {
        int braces = 0;
        int brackets = 0;
        boolean inString = false;

        for (char c : input.toCharArray()) {
            if (c == '"' && (braces > 0 || brackets > 0)) {
                inString = !inString;
            }
            if (!inString) {
                if (c == '{') braces++;
                if (c == '}') braces--;
                if (c == '[') brackets++;
                if (c == ']') brackets--;
            }
        }

        if (braces != 0 || brackets != 0) {
            throw new RuntimeException("Invalid JSON structure");
        }
    }

    public static void main(String[] args) {
        try {
            String json = readJsonFromFile("sample.json");
            JSONParser parser = new JSONParser(json);
            Object result = parser.parse();
            System.out.println(result);
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
    }

    private static String readJsonFromFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}