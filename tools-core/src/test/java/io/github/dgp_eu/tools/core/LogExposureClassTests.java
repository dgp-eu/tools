package io.github.dgp_eu.tools.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * LogExposureClass testing
 */
@DisplayName("LogExposureClass testing")
class LogExposureClassTests {
    /**
     * String for Present
     */
    private static final String STR_ACTIVE = "active";
    /**
     * String for Present
     */
    private static final String STR_PRESENT = "present";

    @Test
    @DisplayName("HandleNameUnformattedMessage testing 1")
    void testHandleNameUnformattedMessage() {
        final String strUnformatted = "Multiple strings are %s";
        final String strExpected = "Multiple strings are present";
        final String handled = LogExposureClass.handleNameUnformattedMessage(1, strUnformatted, STR_PRESENT);
        assertEquals(strExpected, handled, String.format("\"%s\" is not equal to \"%s\"", strUnformatted, strExpected));
    }

    @Test
    @DisplayName("HandleNameUnformattedMessage testing 2")
    void testHandleNameUnformattedMessage2() {
        final String strUnformatted = "Multiple strings are %s and %s";
        final String strExpected = "Multiple strings are present and active";
        final String handled = LogExposureClass.handleNameUnformattedMessage(2, strUnformatted, STR_PRESENT, STR_ACTIVE);
        assertEquals(strExpected, handled, String.format("\"%s\" is not equal to \"%s\"", strUnformatted, strExpected));
    }

    @Test
    @DisplayName("HandleNameUnformattedMessage testing 3")
    void testHandleNameUnformattedMessage3() {
        final String strUnformatted = "Multiple strings are %s, %s and %s";
        final String strExpected = "Multiple strings are present, active and high quality";
        final String handled = LogExposureClass.handleNameUnformattedMessage(3, strUnformatted, STR_PRESENT, STR_ACTIVE, "high quality");
        assertEquals(strExpected, handled, String.format("\"%s\" is not equal to \"%s\"", strUnformatted, strExpected));
    }

    @Test
    @DisplayName("HandleNameUnformattedMessage testing 4 which is to many")
    void testHandleNameUnformattedMessageElse() {
        final String strUnformatted = "Multiple strings are %s, %s, %s and %s";
        final String strExpected = "Multiple strings are present, active, cool and high quality";
        final String exception = LogExposureClass.handleNameUnformattedMessage(4, strUnformatted, STR_PRESENT, STR_ACTIVE, "high", "quality");
        assertNotEquals(strExpected, exception, String.format("\"%s\" is NOT not equal to \"%s\"", strUnformatted, strExpected));
    }

    /**
     * Constructor
     */
    LogExposureClassTests() {
        // intentionally blank
    }

}
