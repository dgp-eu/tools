package io.github.dgp_eu.tools.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Regular Expressions testing
 */
class RegularExpressionsClassTests {
    /**
     * String for Original not equal to Expected
     */
    private static final String ORIG_NQ_EXPCT = "\"%s\" is not equal to \"%s\"";

    @Test
    void testConvertAgingDateIntoHumanReadableString() {
        final String strOriginal = "+0000-01-05";
        final String strExpected = "1 month, 5 days";
        final String handled = RegularExpressionsClass.ConversionSubClass.convertAgingDateIntoHumanReadableString(strOriginal);
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    @Test
    void testConvertAgingTimeIntoHumanReadableString() {
        final String strOriginal = "16:53:09";
        final String strExpected = "16 hours, 53 minutes, 9 seconds";
        final String handled = RegularExpressionsClass.ConversionSubClass.convertAgingTimeIntoHumanReadableString(strOriginal);
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    @Test
    void testReplacePatterns() {
        final String largeContent = """
Started on 2026-03-25.
Log at 2026-03-25 10:00:00.
High precision at 2026-12-25 14:30:05.123.""";
        final String handled = RegularExpressionsClass.replacePatternsWithTimeZones(largeContent);
        final String strExpected = """
Started on Wed, 25 Mar 2026.
Log at Wed, 25 Mar 2026 10:00:00.
High precision at Fri, 25 Dec 2026 14:30:05.123.""";
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    /**
     * Constructor
     */
    public RegularExpressionsClassTests() {
        // intentionally blank
    }

}
