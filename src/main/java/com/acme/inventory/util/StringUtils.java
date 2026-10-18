package com.acme.inventory.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * General-purpose string helpers used across the application.
 *
 * <p>NOTE (L2L): {@code deserializeState} and {@code serializeState} were removed
 * in the L2L[TRANSFORM] pass — they used {@code ObjectInputStream} without a class
 * filter, constituting an RCE gadget (CWE-502).  No callers existed in the codebase
 * at the time of removal.
 */
public class StringUtils {

    private static final Logger log = LoggerFactory.getLogger(StringUtils.class);

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
        if (s == null)            return "";
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
}
