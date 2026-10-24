package at.technikum.application.server.util;

import at.technikum.application.common.exception.BadRequestException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {
    private Json() {
    }

    public static Map<String, Object> parseObject(String json) {
        Object value = new Parser(json == null ? "" : json).parse();
        if (!(value instanceof Map<?, ?> map)) {
            throw new BadRequestException("JSON_OBJECT_REQUIRED");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    public static String requiredString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof String string) || string.isBlank()) {
            throw new BadRequestException(key.toUpperCase() + "_MISSING");
        }
        return string;
    }

    public static String optionalString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (!(value instanceof String string)) {
            throw new BadRequestException(key.toUpperCase() + "_INVALID");
        }
        return string;
    }

    public static int requiredInt(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof Number number)) {
            throw new BadRequestException(key.toUpperCase() + "_MISSING");
        }
        return number.intValue();
    }

    public static List<String> requiredStringList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof List<?> list)) {
            throw new BadRequestException(key.toUpperCase() + "_MISSING");
        }
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof String s) || s.isBlank()) {
                throw new BadRequestException(key.toUpperCase() + "_INVALID");
            }
            result.add(s);
        }
        return result;
    }

    public static String stringify(Object value) {
        StringBuilder builder = new StringBuilder();
        writeValue(builder, value);
        return builder.toString();
    }

    private static void writeValue(StringBuilder builder, Object value) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof String s) {
            writeString(builder, s);
        } else if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
        } else if (value instanceof Map<?, ?> map) {
            builder.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) builder.append(',');
                first = false;
                writeString(builder, String.valueOf(entry.getKey()));
                builder.append(':');
                writeValue(builder, entry.getValue());
            }
            builder.append('}');
        } else if (value instanceof Iterable<?> iterable) {
            builder.append('[');
            boolean first = true;
            for (Object item : iterable) {
                if (!first) builder.append(',');
                first = false;
                writeValue(builder, item);
            }
            builder.append(']');
        } else if (value.getClass().isArray()) {
            builder.append('[');
            for (int i = 0; i < Array.getLength(value); i++) {
                if (i > 0) builder.append(',');
                writeValue(builder, Array.get(value, i));
            }
            builder.append(']');
        } else {
            writeString(builder, String.valueOf(value));
        }
    }

    private static void writeString(StringBuilder builder, String value) {
        builder.append('"');
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (c < 0x20) builder.append(String.format("\\u%04x", (int) c));
                    else builder.append(c);
                }
            }
        }
        builder.append('"');
    }

    private static final class Parser {
        private final String input;
        private int index;

        private Parser(String input) {
            this.input = input;
        }

        private Object parse() {
            skipWhitespace();
            if (index >= input.length()) throw new BadRequestException("JSON_BODY_REQUIRED");
            Object value = parseValue();
            skipWhitespace();
            if (index != input.length()) throw new BadRequestException("JSON_INVALID");
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (index >= input.length()) throw new BadRequestException("JSON_INVALID");
            char c = input.charAt(index);
            return switch (c) {
                case '{' -> parseObjectInternal();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> parseLiteral("true", Boolean.TRUE);
                case 'f' -> parseLiteral("false", Boolean.FALSE);
                case 'n' -> parseLiteral("null", null);
                default -> {
                    if (c == '-' || Character.isDigit(c)) yield parseNumber();
                    throw new BadRequestException("JSON_INVALID");
                }
            };
        }

        private Map<String, Object> parseObjectInternal() {
            Map<String, Object> map = new LinkedHashMap<>();
            expect('{');
            skipWhitespace();
            if (peek('}')) {
                index++;
                return map;
            }
            while (true) {
                skipWhitespace();
                if (!peek('"')) throw new BadRequestException("JSON_INVALID");
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (peek('}')) {
                    index++;
                    break;
                }
                expect(',');
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            expect('[');
            skipWhitespace();
            if (peek(']')) {
                index++;
                return list;
            }
            while (true) {
                list.add(parseValue());
                skipWhitespace();
                if (peek(']')) {
                    index++;
                    break;
                }
                expect(',');
            }
            return list;
        }

        private String parseString() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (index < input.length()) {
                char c = input.charAt(index++);
                if (c == '"') return builder.toString();
                if (c != '\\') {
                    builder.append(c);
                    continue;
                }
                if (index >= input.length()) throw new BadRequestException("JSON_INVALID");
                char escaped = input.charAt(index++);
                switch (escaped) {
                    case '"' -> builder.append('"');
                    case '\\' -> builder.append('\\');
                    case '/' -> builder.append('/');
                    case 'b' -> builder.append('\b');
                    case 'f' -> builder.append('\f');
                    case 'n' -> builder.append('\n');
                    case 'r' -> builder.append('\r');
                    case 't' -> builder.append('\t');
                    case 'u' -> {
                        if (index + 4 > input.length()) throw new BadRequestException("JSON_INVALID");
                        try {
                            builder.append((char) Integer.parseInt(input.substring(index, index + 4), 16));
                        } catch (NumberFormatException ex) {
                            throw new BadRequestException("JSON_INVALID");
                        }
                        index += 4;
                    }
                    default -> throw new BadRequestException("JSON_INVALID");
                }
            }
            throw new BadRequestException("JSON_INVALID");
        }

        private Object parseNumber() {
            int start = index;
            if (peek('-')) index++;
            while (index < input.length() && Character.isDigit(input.charAt(index))) index++;
            boolean decimal = false;
            if (peek('.')) {
                decimal = true;
                index++;
                while (index < input.length() && Character.isDigit(input.charAt(index))) index++;
            }
            if (index < input.length() && (input.charAt(index) == 'e' || input.charAt(index) == 'E')) {
                decimal = true;
                index++;
                if (peek('+') || peek('-')) index++;
                while (index < input.length() && Character.isDigit(input.charAt(index))) index++;
            }
            String number = input.substring(start, index);
            try {
                return decimal ? Double.parseDouble(number) : Long.parseLong(number);
            } catch (NumberFormatException ex) {
                throw new BadRequestException("JSON_INVALID");
            }
        }

        private Object parseLiteral(String literal, Object value) {
            if (!input.startsWith(literal, index)) throw new BadRequestException("JSON_INVALID");
            index += literal.length();
            return value;
        }

        private void expect(char expected) {
            skipWhitespace();
            if (index >= input.length() || input.charAt(index) != expected) {
                throw new BadRequestException("JSON_INVALID");
            }
            index++;
        }

        private boolean peek(char value) {
            return index < input.length() && input.charAt(index) == value;
        }

        private void skipWhitespace() {
            while (index < input.length() && Character.isWhitespace(input.charAt(index))) index++;
        }
    }
}
