/**
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Time methods
 */
public final class TimingClass {
    /** constant for time-stamp SQL-style with seconds */
    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    /** constant for abbreviated time-stamp with seconds */
    public static final String DATE_TIME_ABRV = "EEE, dd MMM yyyy HH:mm:ss";
    /** constant for long time-stamp with seconds */
    public static final String DATE_TIME_LONG = "EEEE, dd MMMM yyyy HH:mm:ss";
    /** constant for time-stamp SQL-style with milliseconds */
    public static final String DATE_TIME_MS = "yyyy-MM-dd HH:mm:ss.SSS";
    /** constant for abbreviated time-stamp with milliseconds */
    public static final String DATE_TIME_MS_ABRV = "EEE, dd MMM yyyy HH:mm:ss.SSS";
    /** constant for long time-stamp with milliseconds */
    public static final String DATE_TIME_MS_LONG = "EEEE, dd MMMM yyyy HH:mm:ss.SSS";
    /** constant for long date */
    public static final String ISO_DATE = "yyyy-MM-dd";
    /** constant for abbreviated date */
    public static final String ISO_DATE_ABRV = "EEE, dd MMM yyyy";
    /** constant for long date */
    public static final String ISO_DATE_LONG = "EEEE, dd MMMM yyyy";
    /** String constant */
    public static final int DAY_MILLISECONDS = 24 * 60 * 60 * 1000;
    /** Map with predefined time format patterns used for duration and time-stamp formatting. */
    private static final Map<String, String> TIME_FORMATS;

    static {
        // Initialize the concurrent map
        final Map<String, String> tempMap = new ConcurrentHashMap<>();
        tempMap.put("DotAndNineDigitNumber", ".%09d");
        tempMap.put(BasicStructuresClass.STR_DOT_THREE, ".%03d");
        tempMap.put(BasicStructuresClass.STR_TM_FRM_SP, " %d %s");
        tempMap.put(BasicStructuresClass.STR_SLMN_TWO, ":%02d");
        tempMap.put(BasicStructuresClass.STR_TWO, "%02d");
        tempMap.put(BasicStructuresClass.STR_TWO_NON_ZERO, "%02d");
        // Make the map unmodifiable
        TIME_FORMATS = Collections.unmodifiableMap(tempMap);
    }

    /**
     * get file last modified date time as human-readable format
     * @param file given file
     * @return String
     */
    @Nullable
    public static String getFileLastModifiedTimeAsHumanReadableFormat(@NonNull final Path file) {
        return getFileLastModifiedTimeAsHumanReadableFormat(file, DATE_TIME_MS_ABRV);
    }

    /**
     * get file last modified date time as human-readable format
     * @param file given file
     * @param outFormat format pattern
     * @return String
     */
    @Nullable
    public static String getFileLastModifiedTimeAsHumanReadableFormat(@NonNull final Path file, @NonNull final String outFormat) {
        String lastModifTime = null;
        try {
            final long modifTime = Files.getLastModifiedTime(file).toMillis();
            // Convert to Instant
            final Instant instant = Instant.ofEpochMilli(modifTime);
            // Convert to LocalDateTime in system default zone
            final LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            final DateTimeFormatter fixedFormatter = DateTimeFormatter.ofPattern(outFormat, Locale.US);
            lastModifTime = dateTime.format(fixedFormatter);
        } catch (IOException ei) {
            final String strFeedback = String.format("Error encountered when attempting to get %s file(s) from %s folder", file.getParent(), file.getFileName());
            LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
        }
        return lastModifTime;
    }

    /**
     * Zone Friendly logic
     * @param zoneId zone identifier
     * @return String
     */
    public static String getFriendlyOffset(final String zoneId) {
        // 1. Get the current offset for the zone
        final ZonedDateTime now = ZonedDateTime.now(ZoneId.of(zoneId));
        final ZoneOffset offset = now.getOffset();
        // 2. Get total seconds and convert to hours/minutes
        final int totalSeconds = offset.getTotalSeconds();
        final int absSeconds = Math.abs(totalSeconds);
        final int hours = absSeconds / 3600;
        final int minutes = absSeconds % 3600 / 60;
        // 3. Determine the sign
        final String sign = totalSeconds >= 0 ? "+" : "-";
        // 4. Return formatted string
        // If minutes are 0, just show the hour (e.g., UTC+5)
        // Otherwise, show hour and minutes (e.g., UTC+05:30)
        return (minutes == 0) 
            ? "UTC%s%02d:00".formatted(sign, hours) 
            : "UTC%s%02d:%02d".formatted(sign, hours, minutes);
    }

    /**
     * Converts a string with ISO 8601 date as input into String w. year
     * and week string + 2 digits week #
     * @param strDateIso8601 date as yyyy-MM-dd (a.k.a. ISO 8601 format type)
     * @return String as year, week string + 2 digits week #
     */
    @NonNull
    public static String getIsoYearWeek(@NonNull final String strDateIso8601) {
        final LocalDate inLocalDate = LocalDate.parse(strDateIso8601);
        return inLocalDate.get(WeekFields.ISO.weekBasedYear()) + "wk"
                + String.format("%02d", inLocalDate.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }

    /**
     * build a LocalDateTime from Strings
     * @param strDateIso8601 input Date
     * @param timeContinuous input Time
     * @return LocalDateTime
     */
    @NonNull
    public static LocalDateTime getLocalDateTimeFromStrings(@NonNull final String strDateIso8601, @NonNull final String timeContinuous) {
        return LocalDateTime.of(
                BasicStructuresClass.convertStringIntoInteger(strDateIso8601.substring(0, 4)),
                BasicStructuresClass.convertStringIntoInteger(strDateIso8601.substring(5, 7)),
                BasicStructuresClass.convertStringIntoInteger(strDateIso8601.substring(8, 10)),
                BasicStructuresClass.convertStringIntoInteger(timeContinuous.substring(0, 2)),
                BasicStructuresClass.convertStringIntoInteger(timeContinuous.substring(2, 4)),
                BasicStructuresClass.convertStringIntoInteger(timeContinuous.substring(4, 6)));
    }

    /**
     * Returns X days ago with milliseconds ago limit
     * @param intDaysLimit number of days in the past
     * @return milliseconds in the past
     */
    public static long getDaysAgoWithMillisecondsPrecision(@NonNull final Instant refTimestamp, final long intDaysLimit) {
        return refTimestamp.minusMillis(intDaysLimit * DAY_MILLISECONDS).toEpochMilli();
    }

    /**
     * Converts a string with ISO 8601 date as input into String as yyyy-MM (MonthName)
     * @param strDateIso8601 date as yyyy-MM-dd (a.k.a. ISO 8601 format type)
     * @return String as yyyy-MM (MonthName)
     */
    @NonNull
    public static String getYearMonthWithFullName(@NonNull final String strDateIso8601) {
        final LocalDate inLocalDate = LocalDate.parse(strDateIso8601);
        return strDateIso8601.substring(0, 7)
                + " ("
                + inLocalDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + ")";
    }

    /**
     * log a duration
     * 
     * @param startTimeStamp times-tamp value seen at start
     * @param finishTimeStamp times-tamp value seen at stop
     * @param strPartial prefix for feedback
     * @return String
     */
    @NonNull
    public static String logDuration(@NonNull final LocalDateTime startTimeStamp, @NonNull final LocalDateTime finishTimeStamp, @NonNull final String strPartial) {
        final ZonedDateTime zStartTimeStamp = ZonedDateTime.of(startTimeStamp, ZoneId.systemDefault());
        final ZonedDateTime zStopTimeStamp = ZonedDateTime.of(finishTimeStamp, ZoneId.systemDefault());
        final Duration objDuration = Duration.between(zStartTimeStamp, zStopTimeStamp);
        return String.format("%s within a duration of %s (which is %s | %s)"
            , strPartial
            , objDuration.toString()
            , ConversionSubClass.convertNanosecondsIntoSomething(objDuration, "HumanReadableTime")
            , ConversionSubClass.convertNanosecondsIntoSomething(objDuration, "TimeClock"));
    }

    /**
     * Converting Time
     */
    public static final class ConversionSubClass {

        /**
         * Convert Nanoseconds to a more digestible string
         * 
         * @param duration actual duration in nanoseconds
         * @param strRule rule to use for conversion
         * @return String
         */
        @NonNull
        public static String convertNanosecondsIntoSomething(@NonNull final Duration duration, @NonNull final String strRule) {
            final StringBuilder strFinalString = new StringBuilder(100);
            final String[] arrayStrings;
            String strFinalOne = null;
            switch (strRule) {
                case "HumanReadableTime":
                    final String strFinalRule = BasicStructuresClass.STR_TM_FRM_SP;
                    arrayStrings = new String[] {strFinalRule, strFinalRule, strFinalRule, strFinalRule};
                    strFinalOne = "Nanosecond";
                    break;
                case "HumanReadableTimeWithMilliseconds":
                    final String strMilliRule = BasicStructuresClass.STR_TM_FRM_SP;
                    arrayStrings = new String[] {strMilliRule, strMilliRule, strMilliRule, strMilliRule};
                    strFinalOne = BasicStructuresClass.STR_MILLISECOND;
                    break;
                case "TimeClockClassic":
                    arrayStrings = new String[] {BasicStructuresClass.STR_TWO_NON_ZERO, BasicStructuresClass.STR_TWO, BasicStructuresClass.STR_SLMN_TWO};
                    break;
                case "TimeClock":
                    arrayStrings = new String[] {BasicStructuresClass.STR_TWO_NON_ZERO, BasicStructuresClass.STR_TWO, BasicStructuresClass.STR_SLMN_TWO, BasicStructuresClass.STR_DOT_THREE};
                    strFinalOne = BasicStructuresClass.STR_MILLISECOND;
                    break;
                default:
                    final String strFeedbackErr = LogExposureClass.getUnsupportedFeatures(strRule, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                    throw new UnsupportedOperationException(strFeedbackErr);
            }
            String strFinalPart = "";
            if (strFinalOne != null) {
                strFinalPart = getDurationWithCustomRules(duration, strFinalOne, arrayStrings[3]);
            }
            return strFinalString.append(getDurationWithCustomRules(duration, "Day", arrayStrings[0]))
                    .append(getDurationWithCustomRules(duration, "Hour", arrayStrings[1]))
                    .append(getDurationWithCustomRules(duration, "Minute", arrayStrings[2]))
                    .append(getDurationWithCustomRules(duration, BasicStructuresClass.STR_SECOND, arrayStrings[2]))
                    .append(strFinalPart)
                    .toString()
                    .trim();
        }

        /**
         * converts a Date from one format to another
         *
         * @param inDate input Date
         * @param inTimeFormat input Time Format
         * @param outTimeFormat output Time Format
         * @return String
         */
        @NonNull
        public static String convertTimeFormat(@NonNull final String inDate, @NonNull final String inTimeFormat, @NonNull final String outTimeFormat) {
            final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(inTimeFormat, Locale.US);
            final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outTimeFormat, Locale.US);
            return convertTimeFormat(inDate, inputFormatter, outputFormatter);
        }

        /**
         * converts a Date from one format to another
         *
         * @param inDate input Date
         * @param inTimeFormat input Time Format
         * @param outTimeFormat output Time Format
         * @return String
         */
        @NonNull
        public static String convertTimeFormat(@NonNull final String inDate, @NonNull final DateTimeFormatter inTimeFormat, @NonNull final DateTimeFormatter outTimeFormat) {
            String outDate = ""; 
            try {
                final ZonedDateTime zonedDateTime;
                if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyDate(inDate)) {
                    zonedDateTime = LocalDate.parse(inDate, inTimeFormat).atStartOfDay(ZoneId.systemDefault());
                } else {
                    zonedDateTime = LocalDateTime.parse(inDate, inTimeFormat).atZone(ZoneId.systemDefault());
                }
                outDate = zonedDateTime.format(outTimeFormat);
            } catch (DateTimeParseException e) {
                final String strFeedback = String.format("Error parsing %s with following details: %s", inDate, Arrays.toString(e.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            } catch (UnsupportedTemporalTypeException e) {
                final String strFeedback = String.format("Unsupported Temporal time for %s with following details: %s", inDate, Arrays.toString(e.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
            return outDate;
        }

        /**
         * get number for Duration
         * 
         * @param duration actual duration in nanoseconds
         * @param strWhichPart which part of Date or Time to use for conversion
         * @return final part of Date or Time
         */
        private static long getDurationPartNumber(@NonNull final Duration duration, @NonNull final String strWhichPart) {
            return switch (strWhichPart) {
                case "Day"                                -> duration.toDaysPart();
                case "Hour"                               -> duration.toHoursPart();
                case BasicStructuresClass.STR_MILLISECOND -> duration.toMillisPart();
                case "Minute"                             -> duration.toMinutesPart();
                case "Nanosecond"                         -> duration.toNanosPart();
                case BasicStructuresClass.STR_SECOND      -> duration.toSecondsPart();
                default -> {
                    final String strFeedbackErr = LogExposureClass.getUnsupportedFeatures(strWhichPart, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                    throw new UnsupportedOperationException(strFeedbackErr);
                }
            };
        }

        /**
         * outputs partial duration
         * 
         * @param duration actual duration in nanoseconds
         * @param strWhichPart which time part to compute
         * @param strHow controls output format
         * @return String
         */
        @NonNull
        private static String getDurationWithCustomRules(@NonNull final Duration duration, @NonNull final String strWhichPart, @NonNull final String strHow) {
            final long lngNumber = getDurationPartNumber(duration, strWhichPart);
            String strReturn = "";
            if (lngNumber > 0
                    || !strHow.endsWith("IfGreaterThanZero")) {
                final String strFormats = TIME_FORMATS.get(strHow);
                if ((strFormats == null) || strFormats.isEmpty()) {
                    final String strFeedbackErr = LogExposureClass.getUnsupportedFeatures(strHow, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                    throw new UnsupportedOperationException(strFeedbackErr);
                }
                if (BasicStructuresClass.STR_TM_FRM_SP.equalsIgnoreCase(strHow)) {
                    final String strPart = lngNumber == 1 ? strWhichPart : strWhichPart + "s";
                    strReturn = String.format(strFormats, lngNumber, strPart);
                } else {
                    strReturn = String.format(strFormats, lngNumber);
                }
            }
            return strReturn;
        }

        /**
         * Constructor
         */
        private ConversionSubClass() {
            // intentionally blank
        }

    }

    /**
     * Time Zones and associated coordinates handler
     */
    public static final class LocalizationSubClass {
        /**
         * Input time zone variable
         */
        private static String inputTimeZone = System.getProperty("user.timezone", "Europe/Bucharest");
        /**
         * Output time zone variable
         */
        private static String outputTimeZone = System.getProperty("user.timezone", "Europe/Bucharest");

        /**
         * Convert time-stamp
         * @param strTimeStamp input Time-stamp
         * @return String converted time-stamp and formated
         */
        public static String convertTimestampFriendly(final String strTimeStamp, final String inputFormat, final String outputFormat) {
            final ZonedDateTime inTimeStamp = convertStringIntoZonedDateTime(strTimeStamp, inputFormat);
            ZonedDateTime outTime = inTimeStamp;
            if (!inputTimeZone.equalsIgnoreCase(outputTimeZone)) {
                outTime = inTimeStamp.withZoneSameInstant(ZoneId.of(outputTimeZone));
            }
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(outputFormat, Locale.US);
            return outTime.format(formatter);
        }

        /**
         * Convert String into Zoned Date Time
         * @param strTimeStamp input time-stamp as String
         * @return ZonedDateTime
         */
        private static ZonedDateTime convertStringIntoZonedDateTime(final String strTimeStamp, final String inputFormat) {
            final LocalDateTime localTime = LocalDateTime.parse(strTimeStamp, 
                    DateTimeFormatter.ofPattern(inputFormat, Locale.US));
            return localTime.atZone(ZoneId.of(inputTimeZone));
        }

        /**
         * format date
         * @param strDate input date
         * @return String formated date
         */
        public static String formatDateFriendly(final String strDate, final String inputFormat, final String outputFormat) {
            final LocalDate outDate = LocalDate.parse(strDate, DateTimeFormatter.ofPattern(inputFormat, Locale.US));
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(outputFormat, Locale.US);
            return outDate.format(formatter);
        }

        /**
         * Setter for inputTimeZone
         * @param strTimeZone desired time zone for input
         */
        public static void setInputTimeZone(final String strTimeZone) {
            inputTimeZone = strTimeZone;
        }

        /**
         * Setter for outputTimeZone
         * @param strTimeZone desired time zone for output
         */
        public static void setOutputTimeZone(final String strTimeZone) {
            outputTimeZone = strTimeZone;
        }

        /**
         * Constructor
         */
        private LocalizationSubClass() {
            // intentionally blank
        }

    }

    /**
     * Constructor
     */
    private TimingClass() {
        // intentionally blank
    }
}
