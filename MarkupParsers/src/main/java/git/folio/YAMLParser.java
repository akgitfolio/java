package git.folio;

import java.io.*;
import java.util.*;

public class YAMLParser {
    private String input;
    private int position;
    private int indentation;

    public YAMLParser(String input) {
        this.input = input;
        this.position = 0;
        this.indentation = 0;
    }

    public static Object parseFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        YAMLParser parser = new YAMLParser(content.toString());
        return parser.parse();
    }

    public Object parse() {
        skipWhitespaceAndComments();
        if (peek() == '-') {
            return parseList();
        } else {
            return parseMap();
        }
    }

    private Map<String, Object> parseMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        int currentIndent = indentation;

        while (position < input.length()) {
            skipWhitespaceAndComments();
            if (getIndentation() < currentIndent) {
                break;
            }

            String key = parseKey();
            if (key.isEmpty()) {
                continue;
            }
            consume(':');
            skipWhitespaceAndComments();
            Object value = parseValue();
            map.put(key, value);
        }

        return map;
    }

    private List<Object> parseList() {
        List<Object> list = new ArrayList<>();
        int currentIndent = indentation;

        while (position < input.length()) {
            skipWhitespaceAndComments();
            if (getIndentation() < currentIndent) {
                break;
            }
            if (peek() == '-') {
                consume('-');
                skipWhitespaceAndComments();
                list.add(parseValue());
            } else {
                break;
            }
        }

        return list;
    }

    private String parseKey() {
        StringBuilder sb = new StringBuilder();
        while (position < input.length() && peek() != ':' && peek() != '\n') {
            sb.append(consume());
        }
        return sb.toString().trim();
    }

    private Object parseValue() {
        skipWhitespaceAndComments();
        char c = peek();
        if (c == '-') {
            return parseList();
        } else if (c == '{') {
            return parseInlineMap();
        } else if (c == '[') {
            return parseInlineList();
        } else if (c == '"' || c == '\'') {
            return parseQuotedString();
        } else if (c == '|' || c == '>') {
            return parseMultilineString();
        } else if (c == '\n') {
            return null;
        } else {
            int oldIndentation = indentation;
            indentation = getIndentation() + 2;
            Object result = isComplexValue() ? parseMap() : parseScalar();
            indentation = oldIndentation;
            return result;
        }
    }

    private Map<String, Object> parseInlineMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        consume('{');
        while (peek() != '}') {
            skipWhitespaceAndComments();
            String key = parseKey();
            consume(':');
            skipWhitespaceAndComments();
            Object value = parseValue();
            map.put(key, value);
            if (peek() == ',') {
                consume(',');
                skipWhitespaceAndComments();
            }
        }
        consume('}');
        return map;
    }

    private List<Object> parseInlineList() {
        List<Object> list = new ArrayList<>();
        consume('[');
        while (peek() != ']') {
            skipWhitespaceAndComments();
            list.add(parseValue());
            if (peek() == ',') {
                consume(',');
                skipWhitespaceAndComments();
            }
        }
        consume(']');
        return list;
    }

    private String parseQuotedString() {
        char quote = consume();
        StringBuilder sb = new StringBuilder();
        while (peek() != quote) {
            if (peek() == '\\') {
                consume();
                sb.append(parseEscapedChar());
            } else {
                sb.append(consume());
            }
        }
        consume(quote);
        return sb.toString();
    }

    private char parseEscapedChar() {
        char c = consume();
        switch (c) {
            case 'n': return '\n';
            case 't': return '\t';
            case 'r': return '\r';
            default: return c;
        }
    }

    private String parseMultilineString() {
        char style = consume(); // '|' or '>'
        consume('\n');
        StringBuilder sb = new StringBuilder();
        int startIndent = getIndentation();

        while (position < input.length()) {
            int currentIndent = getIndentation();
            if (currentIndent < startIndent) {
                break;
            }
            String line = parseToEndOfLine().substring(startIndent);
            sb.append(line);
            if (style == '|' || position >= input.length()) {
                sb.append('\n');
            } else {
                sb.append(' ');
            }
        }

        return sb.toString().trim();
    }

    private Object parseScalar() {
        String value = parseToEndOfLine().trim();
        if (value.equalsIgnoreCase("true")) return true;
        if (value.equalsIgnoreCase("false")) return false;
        if (value.equalsIgnoreCase("null")) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e1) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e2) {
                return value;
            }
        }
    }

    private boolean isComplexValue() {
        int savedPosition = position;
        skipWhitespaceAndComments();
        boolean result = peek() == '-' || (position < input.length() - 1 && input.charAt(position + 1) == ':');
        position = savedPosition;
        return result;
    }

    private String parseToEndOfLine() {
        StringBuilder sb = new StringBuilder();
        while (position < input.length() && peek() != '\n') {
            sb.append(consume());
        }
        if (position < input.length()) {
            consume(); // consume newline
        }
        return sb.toString();
    }

    private char peek() {
        return position < input.length() ? input.charAt(position) : '\0';
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

    private void skipWhitespaceAndComments() {
        while (position < input.length()) {
            if (Character.isWhitespace(peek())) {
                if (peek() == '\n') {
                    indentation = 0;
                }
                position++;
            } else if (peek() == '#') {
                while (position < input.length() && peek() != '\n') {
                    position++;
                }
            } else {
                break;
            }
        }
    }

    private int getIndentation() {
        int indent = 0;
        int i = position;
        while (i < input.length() && input.charAt(i) == ' ') {
            indent++;
            i++;
        }
        return indent;
    }

    public static void main(String[] args) {
        try {
            Object result = YAMLParser.parseFile("sample.yaml");
            System.out.println(result);
        } catch (IOException e) {
            System.err.println("Error reading YAML file: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error parsing YAML: " + e.getMessage());
            e.printStackTrace();
        }
    }
}