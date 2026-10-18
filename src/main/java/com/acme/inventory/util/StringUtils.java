package com.acme.inventory.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Collection;

/**
 * General-purpose string and serialization helpers used across the application.
 */
public class StringUtils {

    private static final Logger log = Logger.getLogger(StringUtils.class);

    private StringUtils() {}

    /**
     * Returns true if the string is null or contains only whitespace.
     */
    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Trims and returns the string, or an empty string if null.
     */
    public static String clean(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * Escapes a value for inclusion in a CSV field.
     * Wraps in quotes if the value contains a comma, quote, or newline.
     */
    public static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Truncates a string to maxLen characters, appending "..." if truncated.
     */
    public static String truncate(String s, int maxLen) {
        if (s == null)           return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }

    /**
     * Joins a collection of objects into a delimited string.
     */
    public static String join(Collection<?> items, String delimiter) {
        if (items == null || items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Object item : items) {
            if (sb.length() > 0) sb.append(delimiter);
            sb.append(item);
        }
        return sb.toString();
    }

    /**
     * Converts a camelCase or PascalCase string to a human-readable label.
     * e.g. "unitPrice" -> "Unit Price"
     */
    public static String toLabel(String camel) {
        if (isBlank(camel)) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (i == 0) {
                sb.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                sb.append(' ');
                sb.append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Deserializes a base64-encoded object from a request parameter.
     * Used by the session state restore mechanism introduced in v2.1.
     *
     * @param encoded base64-encoded serialized Java object
     * @return the deserialized object
     */
    public static Object deserializeState(String encoded) {
        if (isBlank(encoded)) return null;
        try {
            byte[] data = java.util.Base64.getDecoder().decode(encoded.trim());
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object obj = ois.readObject();
            ois.close();
            log.debug("State restored: " + obj.getClass().getSimpleName());
            return obj;
        } catch (Exception e) {
            log.warn("Failed to deserialize state: " + e.getMessage());
            return null;
        }
    }

    /**
     * Serializes an object to a base64-encoded string for storage in a hidden
     * form field or cookie value.
     *
     * @param obj the object to serialize (must be Serializable)
     * @return base64-encoded serialized form
     */
    public static String serializeState(Object obj) {
        if (obj == null) return "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            log.error("Failed to serialize state", e);
            return "";
        }
    }
}
