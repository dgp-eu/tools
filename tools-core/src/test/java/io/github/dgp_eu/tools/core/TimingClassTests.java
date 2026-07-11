package io.github.dgp_eu.tools.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TimingClass}, covering ISO year/week formatting, date/time
 * conversions, duration logging, and localized time-stamp pattern replacement.
 */
@DisplayName("TimingClass unit testing")
class TimingClassTests {
    /** String format for assertion when actual/original is not equal to expected */
    private static final String ORIG_NQ_EXPCT = "calculated \"%s\" is not equal to expected \"%s\"";
    /** fixed Clock for predictable results */
    private static final java.time.Clock CLOCK_FIXED = java.time.Clock.fixed(Instant.parse("2022-12-12T22:22:22Z"), java.time.ZoneId.of("UTC"));

    @Test
    void testConvertTimestampFriendly() {
        TimingClass.LocalizationSubClass.setInputTimeZone("UTC");
        TimingClass.LocalizationSubClass.setOutputTimeZone("America/New_York");
        final String strOriginal = "2026-02-08 15:09:34";
        final String handled = TimingClass.LocalizationSubClass.convertTimestampFriendly(strOriginal, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss");
        assertNotEquals(strOriginal, handled, String.format(ORIG_NQ_EXPCT, handled, strOriginal));
    }

    @Test
    void testGetFileLastModifiedTimeAsHumanReadableFormat() throws IOException {
        final Path tempFile = Files.createTempFile("test-file-", ".txt");
        final String handled = TimingClass.LocalizationSubClass.FileSubSubClass.getFileLastModifiedTimeAsHumanReadableFormat(tempFile);
        assertTrue(BasicStructuresClass.StringEvaluationSubClass.isStringActuallyLongTimestampWithMilliseconds(handled),
                "Last Modified timestamp is not a long timestamp with milliseconds");
    }

    @Test
    void testGetIsoYearWeek() {
        final String strOriginal = "2026-02-08";
        final String strExpected = "2026wk06";
        final String handled = TimingClass.getIsoYearWeek(strOriginal);
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    @Test
    void testGetDaysAgoWithMillisecondsPrecision() {
        final Instant startNow = Instant.now(CLOCK_FIXED);
        final long expected = startNow.minusMillis(TimingClass.DAY_MILLISECONDS).toEpochMilli();
        final long handled = TimingClass.getDaysAgoWithMillisecondsPrecision(startNow, 1);
        assertEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    void testGetDaysAgoWithMillisecondsPrecisionZeroDays() {
        final Instant startNow = Instant.now(CLOCK_FIXED);
        final long expected = startNow.toEpochMilli();
        final long handled = TimingClass.getDaysAgoWithMillisecondsPrecision(startNow, 0);
        assertEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    void testGetDaysAgoWithMillisecondsPrecisionNegativeDays() {
        final Instant startNow = Instant.now(CLOCK_FIXED);
        final int intDaysLimit = -1;
        final long expected = startNow.minusMillis((long) TimingClass.DAY_MILLISECONDS * intDaysLimit).toEpochMilli();
        final long handled = TimingClass.getDaysAgoWithMillisecondsPrecision(startNow, intDaysLimit);
        assertEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    void testGetDaysAgoWithMillisecondsPrecisionLargeDays() {
        final Instant startNow = Instant.now(CLOCK_FIXED);
        final int intDaysLimit = 30;
        final long expected = startNow.minusMillis((long) TimingClass.DAY_MILLISECONDS * intDaysLimit).toEpochMilli();
        final long handled = TimingClass.getDaysAgoWithMillisecondsPrecision(startNow, intDaysLimit);
        assertEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    void testGetLocalDateTimeFromStrings() {
        final String strDateIso8601 = "2026-02-08";
        final String timeContinuous = "150934";
        final LocalDateTime handled = TimingClass.getLocalDateTimeFromStrings(strDateIso8601, timeContinuous);
        final LocalDateTime expected = LocalDateTime.of(2026, Month.FEBRUARY, 8, 15, 9, 34);
        assertEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    void testGetYearMonthWithFullName() {
        final String strOriginal = "2026-02-08";
        final String strExpected = "2026-02 (February)";
        final String handled = TimingClass.getYearMonthWithFullName(strOriginal);
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    @Test
    void testLogDuration() {
        final Instant startNow = Instant.now(CLOCK_FIXED);
        final LocalDateTime startTimeStamp = LocalDateTime.ofInstant(startNow.minusSeconds(33), ZoneOffset.systemDefault());
        final LocalDateTime finishTimeStamp = LocalDateTime.ofInstant(startNow, ZoneOffset.systemDefault());
        final String strExpected = String.format("Finished within a duration of %s (which is %s | %s)", "PT33S", "33 Seconds", "00:00:33.000");
        final String handled = TimingClass.logDuration(startTimeStamp, finishTimeStamp, "Finished");
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    @Test
    void testLogDuration2() {
        final Instant startNow = Instant.now(CLOCK_FIXED);
        final LocalDateTime startTimeStamp = LocalDateTime.ofInstant(startNow.minusSeconds(60 * 60).minusSeconds(33), ZoneOffset.systemDefault());
        final LocalDateTime finishTimeStamp = LocalDateTime.ofInstant(startNow, ZoneOffset.systemDefault());
        final String strExpected = String.format("Finished within a duration of %s (which is %s | %s)", "PT1H33S", "1 Hour 33 Seconds", "01:00:33.000");
        final String handled = TimingClass.logDuration(startTimeStamp, finishTimeStamp, "Finished");
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    /**
     * Constructor
     */
    TimingClassTests() {
        // intentionally blank
    }

}
