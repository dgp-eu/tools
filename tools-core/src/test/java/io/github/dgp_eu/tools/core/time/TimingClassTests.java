package io.github.dgp_eu.tools.core.time;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.dgp_eu.tools.core.BasicStructuresClass;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TimingClass}, covering ISO year/week formatting, date/time
 * conversions, duration logging, and localized time-stamp pattern replacement.
 */
@DisplayName("TimingClass unit testing")
class TimingClassTests {
    /** String format for assertion when actual/original is not equal to expected */
    private static final String ORIG_NQ_EXPCT = "calculated \"%s\" is not equal to expected \"%s\"";
    /** String format for assertion when actual/original 
     * is not equal to expected for Aging */
    private static final String AGING_ERR = "calculated \"%s\" is not equal to expected \"%s\" considering %s as start and %s as finish having a Period of %s and Duration of %s";
    /** fixed Clock for predictable results */
    private static final ZoneId CLOCK_TZ = ZoneId.of("UTC");
    /** fixed Clock for predictable results */
    private static final Clock CLOCK_FIXED = Clock.fixed(Instant.parse("2023-08-26T22:57:42Z"), CLOCK_TZ);

    /**
     * Constructor
     */
    private TimingClassTests() {
        super();
    }

    @Test
    void testAgingNegative() {
        final Instant startNow = Instant.now(CLOCK_FIXED);
        final ZonedDateTime startDateTime = ZonedDateTime.ofInstant(startNow, CLOCK_TZ);
        final ZonedDateTime finishDateTime = ZonedDateTime.ofInstant(startNow.minus(3, ChronoUnit.HOURS).minus(4, ChronoUnit.MINUTES).minus(5, ChronoUnit.SECONDS).minus(6, ChronoUnit.MILLIS), CLOCK_TZ);
        final String handled = TimingClass.AgingSubClass.computeAgingIntoHumanReadableWords(startDateTime, finishDateTime);
        final String expected = "-3 hours 4 minutes 5 seconds 6 milliseconds";
        final Period period = Period.between(startDateTime.toLocalDate(), finishDateTime.toLocalDate());
        final ZonedDateTime startAfterPeriod = startDateTime.plus(period);
        final Duration duration = Duration.between(startAfterPeriod, finishDateTime);
        assertEquals(expected, handled, String.format(AGING_ERR, handled, expected, startDateTime, finishDateTime, period, duration));
    }

    @Test
    void testAgingNegative2() {
        final Instant startNow = Instant.now(CLOCK_FIXED);
        final ZonedDateTime startDateTime = ZonedDateTime.ofInstant(startNow, CLOCK_TZ);
        final ZonedDateTime finishDateTime = ZonedDateTime.ofInstant(startNow.minus(62, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS), CLOCK_TZ);
        final String handled = TimingClass.AgingSubClass.computeAgingIntoHumanReadableWords(startDateTime, finishDateTime);
        final String expected = "-2 months 20 hours";
        final Period period = Period.between(startDateTime.toLocalDate(), finishDateTime.toLocalDate());
        final ZonedDateTime startAfterPeriod = startDateTime.plus(period);
        final Duration duration = Duration.between(startAfterPeriod, finishDateTime);
        assertEquals(expected, handled, String.format(AGING_ERR, handled, expected, startDateTime, finishDateTime, period, duration));
    }

    @Test
    void testAgingPositive() {
        final Instant startNow = Instant.now(CLOCK_FIXED);
        final ZonedDateTime startDateTime = ZonedDateTime.ofInstant(startNow, CLOCK_TZ);
        final ZonedDateTime finishDateTime = ZonedDateTime.ofInstant(startNow.plus(3, ChronoUnit.DAYS), CLOCK_TZ);
        final String handled = TimingClass.AgingSubClass.computeAgingIntoHumanReadableWords(startDateTime, finishDateTime);
        final String expected = "3 days";
        final Period period = Period.between(startDateTime.toLocalDate(), finishDateTime.toLocalDate());
        final ZonedDateTime startAfterPeriod = startDateTime.plus(period);
        final Duration duration = Duration.between(startAfterPeriod, finishDateTime);
        assertEquals(expected, handled, String.format(AGING_ERR, handled, expected, startDateTime, finishDateTime, period, duration));
    }

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
        final String strExpected = String.format("Finished within a duration of %s (which is %s | %s)", "PT33S", "33 seconds", "00:00:33.000");
        final String handled = TimingClass.logDuration(startTimeStamp, finishTimeStamp, "Finished");
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    @Test
    void testLogDuration2() {
        final Instant startNow = Instant.now(CLOCK_FIXED);
        final LocalDateTime startTimeStamp = LocalDateTime.ofInstant(startNow.minusSeconds(60 * 60).minusSeconds(33), ZoneOffset.systemDefault());
        final LocalDateTime finishTimeStamp = LocalDateTime.ofInstant(startNow, ZoneOffset.systemDefault());
        final String strExpected = String.format("Finished within a duration of %s (which is %s | %s)", "PT1H33S", "1 hour 33 seconds", "01:00:33.000");
        final String handled = TimingClass.logDuration(startTimeStamp, finishTimeStamp, "Finished");
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    /**
     * Test for StringCleaningClass
     */
    @Nested
    /* default */ @DisplayName("getDaysAgoWithMillisecondsPrecision testing...")
    final class TestDaysAgoSubClass {

        /**
         * Constructor
         */
        private TestDaysAgoSubClass() {
            super();
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

    }

}
