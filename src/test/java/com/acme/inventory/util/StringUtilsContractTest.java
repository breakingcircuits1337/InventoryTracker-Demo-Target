package com.acme.inventory.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract tests for {@link StringUtils} — Module 1, L2L[VERIFY].
 *
 * Covers contracts SU-01 through SU-06.
 * SU-07 and SU-08 are enforced at compile time: this file does not reference
 * deserializeState or serializeState, and the project must compile cleanly.
 */
class StringUtilsContractTest {

    // ------------------------------------------------------------------
    // SU-01  isBlank
    // ------------------------------------------------------------------

    @Test
    void isBlank_null_returnsTrue() {
        assertTrue(StringUtils.isBlank(null));
    }

    @Test
    void isBlank_emptyString_returnsTrue() {
        assertTrue(StringUtils.isBlank(""));
    }

    @Test
    void isBlank_whitespaceOnly_returnsTrue() {
        assertTrue(StringUtils.isBlank("   "));
    }

    @Test
    void isBlank_nonBlank_returnsFalse() {
        assertFalse(StringUtils.isBlank("x"));
    }

    // ------------------------------------------------------------------
    // SU-02  clean
    // ------------------------------------------------------------------

    @Test
    void clean_null_returnsEmptyString() {
        assertEquals("", StringUtils.clean(null));
    }

    @Test
    void clean_paddedString_returnsTrimmed() {
        assertEquals("hi", StringUtils.clean("  hi  "));
    }

    @Test
    void clean_emptyString_returnsEmptyString() {
        assertEquals("", StringUtils.clean(""));
    }

    // ------------------------------------------------------------------
    // SU-03  escapeCsv
    // ------------------------------------------------------------------

    @Test
    void escapeCsv_null_returnsEmptyString() {
        assertEquals("", StringUtils.escapeCsv(null));
    }

    @Test
    void escapeCsv_plainValue_returnedAsIs() {
        assertEquals("hello", StringUtils.escapeCsv("hello"));
    }

    @Test
    void escapeCsv_valueWithComma_wrappedInQuotes() {
        assertEquals("\"a,b\"", StringUtils.escapeCsv("a,b"));
    }

    @Test
    void escapeCsv_valueWithDoubleQuote_wrappedAndQuoteDoubled() {
        assertEquals("\"say \"\"hi\"\"\"", StringUtils.escapeCsv("say \"hi\""));
    }

    @Test
    void escapeCsv_valueWithNewline_wrappedInQuotes() {
        String result = StringUtils.escapeCsv("line1\nline2");
        assertTrue(result.startsWith("\"") && result.endsWith("\""));
    }

    // ------------------------------------------------------------------
    // SU-04  truncate
    // ------------------------------------------------------------------

    @Test
    void truncate_longString_appendsEllipsis() {
        assertEquals("a...", StringUtils.truncate("abcde", 4));
    }

    @Test
    void truncate_shortString_returnedUnchanged() {
        assertEquals("abc", StringUtils.truncate("abc", 5));
    }

    @Test
    void truncate_null_returnsEmptyString() {
        assertEquals("", StringUtils.truncate(null, 5));
    }

    @Test
    void truncate_exactLength_returnedUnchanged() {
        assertEquals("abcd", StringUtils.truncate("abcd", 4));
    }

    // ------------------------------------------------------------------
    // SU-05  join
    // ------------------------------------------------------------------

    @Test
    void join_multipleItems_returnsDelimited() {
        assertEquals("a,b,c", StringUtils.join(Arrays.asList("a", "b", "c"), ","));
    }

    @Test
    void join_emptyCollection_returnsEmptyString() {
        assertEquals("", StringUtils.join(Collections.emptyList(), ","));
    }

    @Test
    void join_null_returnsEmptyString() {
        assertEquals("", StringUtils.join(null, ","));
    }

    @Test
    void join_singleItem_returnsItemWithNoDelimiter() {
        assertEquals("only", StringUtils.join(Collections.singletonList("only"), ","));
    }

    // ------------------------------------------------------------------
    // SU-06  toLabel
    // ------------------------------------------------------------------

    @Test
    void toLabel_camelCase_insertsSpacesBeforeUppercase() {
        assertEquals("Unit Price", StringUtils.toLabel("unitPrice"));
    }

    @Test
    void toLabel_null_returnsEmptyString() {
        assertEquals("", StringUtils.toLabel(null));
    }

    @Test
    void toLabel_emptyString_returnsEmptyString() {
        assertEquals("", StringUtils.toLabel(""));
    }

    @Test
    void toLabel_allCaps_insertsSpacesBetweenEachLetter() {
        // "SKU" → first char uppercased is 'S', then ' K', then ' U'
        assertEquals("S K U", StringUtils.toLabel("SKU"));
    }
}
