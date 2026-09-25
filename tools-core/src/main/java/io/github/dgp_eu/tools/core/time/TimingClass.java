/** Copyright 2026 Daniel-Gheorghe Popiniuc */
package io.github.dgp_eu.tools.core.time;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.ConfigurationClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.core.RegularExpressionsClass;


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
    /** constant for time SQL-style with milliseconds */
    public static final String TIME_MS = "HH:mm:ss.SSS";
    /** constant for time number style with milliseconds */
    public static final String TIME_NO = "HHmmss.SSS";
    /** String constant */
    public static final int DAY_MILLISECONDS = 24 * 60 * 60 * 1000;
    /** Record for Aging Components */
    /* default */ public record AgingInfoRecord(
        boolean isNegative,
        Integer intYears,
        Integer intMonths,
        Integer intDays,
        Integer intHours,
        Integer intMinutes,
        Integer intSeconds,
        Integer intMilliseconds) {}

    /**
     * Current local DateTime w. TZ as String
     * @return String
     */
    public static String getCurrentDateTimeLocal() {
        return DateTimeFormatter.ofPattern(DATE_TIME_MS_ABRV, Locale.US)
                .format(ZonedDateTime.now(ZoneId.systemDefault()));
    }

    /**
     * Current local DateTime w. TZ as String
     * @return String
     */
    public static String getCurrentDateTimeLocal(final String inTimeZone) {
        return DateTimeFormatter.ofPattern(DATE_TIME_MS_ABRV, Locale.US)
                .format(ZonedDateTime.now(ZoneId.of(inTimeZone)));
    }

    /**
     * Current DateTime
     * @return String
     */
    public static String getCurrentDateTimeLocalRaw() {
        return DateTimeFormatter.ofPattern(DATE_TIME_MS, Locale.US)
                .format(ZonedDateTime.now(ZoneId.systemDefault()));
    }

    /**
     * Current DateTime UTC
     * @return String
     */
    public static String getCurrentDateTimeUniveralTimeCoordination() {
        return DateTimeFormatter.ofPattern(DATE_TIME_MS, Locale.US)
                .format(ZonedDateTime.now(Clock.systemUTC()));
    }

    /**
     * Current local DateTime w. TZ as String
     * @return String
     */
    public static ZonedDateTime getCurrentZonedDateTime() {
        return ZonedDateTime.now(ZoneId.systemDefault());
    }

    /**
     * Epoch Milliseconds as String
     * @param cutoff input reference TS as Long value
     * @return String
     */
    public static String getEpochMilliseconds(final long cutoff) {
        return Instant.ofEpochMilli(cutoff).toString().replaceAll("[TZ]", " ").trim();
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
    public static long getDaysAgoWithMillisecondsPrecision(final long intDaysLimit) {
        return Instant.now().minusMillis(intDaysLimit * DAY_MILLISECONDS).toEpochMilli();
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
        return logDuration(zStartTimeStamp, zStopTimeStamp, strPartial);
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
    public static String logDuration(@NonNull final ZonedDateTime startTimeStamp, @NonNull final ZonedDateTime finishTimeStamp, @NonNull final String strPartial) {
        final Duration objDuration = Duration.between(startTimeStamp, finishTimeStamp);
        return String.format("%s within a duration of %s (which is %s | %s)"
            , strPartial
            , objDuration.toString()
            , AgingSubClass.computeAgingIntoHumanReadableWords(startTimeStamp, finishTimeStamp)
            , AgingSubClass.computeAgingIntoTimeClock(startTimeStamp, finishTimeStamp));
    }

    /**
     * Time Zones and associated coordinates handler
     */
    public static final class AgingSubClass {

        /**
         * Constructor
         */
        private AgingSubClass() {
            // intentionally blank
        }

        /**
         * append w. prefix if value is not 0
         * @param parts input list to alter
         * @param value input value to evaluate
         * @param strPrefix prefix of the value
         */
        private static void appendValueWithPrefixIfNotZero(final List<String> parts, final long value, final String strPrefix, final String strMeaning) {
            String zeroString = "00";
            String nonZeroFormat = "%02d";
            if (ConfigurationClass.STR_MILLISECONDS.equalsIgnoreCase(strMeaning)) {
                zeroString = "000";
                nonZeroFormat = "%03d";
            }
            if (value == 0) {
                parts.add(strPrefix + zeroString);
            } else {
                parts.add(strPrefix + String.format(nonZeroFormat, value));
            }
        }

        /**
         * append w. units if value is not 0
         * @param parts input list to alter
         * @param value input value to evaluate
         * @param unit unit of the value
         */
        private static void appendValueWithUnitsIfNotZero(final List<String> parts, final long value, final String unit) {
            if (value != 0) {
                parts.add(value + " " + unit + (Math.abs(value) == 1 ? "" : "s"));
            }
        }

        /**
         * composing Aging as words from Integer components
         * @param inAgeComponents age components as List of Integer values
         * @param strZeroValue return value if all parts are empty
         * @return String with words for aging
         */
        public static String composeAgingClockFromListOfIntegerComponents(final AgingInfoRecord inAgeComponents, final String strZeroValue) {
            final List<String> parts = new ArrayList<>();
            appendValueWithPrefixIfNotZero(parts, inAgeComponents.intHours, "", "Hours");
            appendValueWithPrefixIfNotZero(parts, inAgeComponents.intMinutes, ":", "Minutes");
            appendValueWithPrefixIfNotZero(parts, inAgeComponents.intSeconds, ":", "Seconds");
            appendValueWithPrefixIfNotZero(parts, inAgeComponents.intMilliseconds, ".", "Milliseconds");
            String strReturn = strZeroValue;
            if (!parts.isEmpty()) {
                strReturn = (inAgeComponents.isNegative ? "-" : "") + String.join("", parts);
            }
            return strReturn;
        }

        /**
         * composing Aging as words from Integer components
         * @param inAgeComponents age components as List of Integer values
         * @param strZeroValue return value if all parts are empty
         * @return String with words for aging
         */
        public static String composeAgingInWordsFromListOfIntegerComponents(final AgingInfoRecord inAgeComponents, final String strZeroValue) {
            final List<String> parts = new ArrayList<>();
            appendValueWithUnitsIfNotZero(parts, inAgeComponents.intYears, "year");
            appendValueWithUnitsIfNotZero(parts, inAgeComponents.intMonths, "month");
            appendValueWithUnitsIfNotZero(parts, inAgeComponents.intDays, "day");
            appendValueWithUnitsIfNotZero(parts, inAgeComponents.intHours, "hour");
            appendValueWithUnitsIfNotZero(parts, inAgeComponents.intMinutes, "minute");
            appendValueWithUnitsIfNotZero(parts, inAgeComponents.intSeconds, "second");
            appendValueWithUnitsIfNotZero(parts, inAgeComponents.intMilliseconds, "millisecond");
            String strReturn = strZeroValue;
            if (!parts.isEmpty()) {
                strReturn = (inAgeComponents.isNegative ? "-" : "") + String.join(" ", parts);
            }
            return strReturn;
        }

        /**
         * Aging logic 
         * @param startTimestamp reference ZonedDatetime
         * @param finishTimestamp ending ZonedDateTime
         * @return Aging
         */
        public static String computeAgingIntoHumanReadableWords(final ZonedDateTime startTimestamp, final ZonedDateTime finishTimestamp) {
            final AgingInfoRecord ageComponents = computeAgingInfoRecord(startTimestamp, finishTimestamp);
            final String strFeedback = String.format("Age components are %s", ageComponents);
            LogExposureClass.LOGGER.debug(strFeedback);
            return composeAgingInWordsFromListOfIntegerComponents(ageComponents, "INSTANT (less than 1 millisecond)");
        }

        /**
         * Aging logic 
         * @param startTimestamp reference ZonedDatetime
         * @param finishTimestamp ending ZonedDateTime
         * @return Aging
         */
        public static String computeAgingIntoTimeClock(final ZonedDateTime startTimestamp, final ZonedDateTime finishTimestamp) {
            final AgingInfoRecord ageComponents = computeAgingInfoRecord(startTimestamp, finishTimestamp);
            return composeAgingClockFromListOfIntegerComponents(ageComponents, "INSTANT (less than 1 millisecond)");
        }

        /**
         * Aging Info Record logic 
         * @param startTimestamp reference ZonedDatetime
         * @param finishTimestamp ending ZonedDateTime
         * @return Aging components
         */
        private static AgingInfoRecord computeAgingInfoRecord(final ZonedDateTime startTimestamp, final ZonedDateTime finishTimestamp) {
            final boolean isNegative             = startTimestamp.isAfter(finishTimestamp);
            final Period period                  = Period.between(startTimestamp.toLocalDate(), finishTimestamp.toLocalDate());
            final Duration duration              = Duration.between(startTimestamp.toInstant(), finishTimestamp.toInstant());
            final String strFeedback = String.format("Period is %s and Duration is %s", period, duration);
            LogExposureClass.LOGGER.debug(strFeedback);
            final int years  = Math.abs(period.getYears());
            final int months = Math.abs(period.getMonths());
            int days   = Math.abs(period.getDays());
            final DateTimeFormatter formatterNo = DateTimeFormatter.ofPattern(TIME_NO, Locale.US);
            final BigDecimal startTimeNumber = new BigDecimal(startTimestamp.format(formatterNo));
            final BigDecimal finishTimeNumber = new BigDecimal(finishTimestamp.format(formatterNo));
            if ("P1D".equalsIgnoreCase(period.toString())
                    && startTimeNumber.compareTo(finishTimeNumber) > 0) {
                final String strFeedback3 = String.format("Start Time as Number is %s and Finish Time as Number is %s within consecutive days, hence a correction of 1 day will take place", startTimeNumber, finishTimeNumber);
                LogExposureClass.LOGGER.debug(strFeedback3);
                days = days - 1;
            }
            // duration components
            final int intHours             = Math.abs(duration.toHoursPart());
            final int intMinutes           = Math.abs(duration.toMinutesPart());
            final String strDuration       = duration.toString().replace("PT", "");
            final List<String> strSeconds  = RegularExpressionsClass.extractMatches(strDuration, "([-+]?[0-9]{1,2}(|[.,][0-9]{0,9}))S");
            int intSeconds = 0;
            int intMilli   = 0;
            if (!strSeconds.isEmpty()) {
                final String firstSecond   = strSeconds.getFirst();
                if (firstSecond.contains(".")) {
                    final String[] firstSecondParts = firstSecond.split("[.,]");
                    intSeconds                      = Math.abs(Integer.parseInt(firstSecondParts[0]));
                    final String strMilliseconds    = firstSecondParts[1].replace("S", "");
                    final int roundingFactor        = strMilliseconds.length() - 3;
                    intMilli                        = new BigDecimal(strMilliseconds).scaleByPowerOfTen(-roundingFactor).intValue();
                } else {
                    intSeconds                      = Math.abs(Integer.parseInt(firstSecond.replace("S", "")));
                }
            }
            return new AgingInfoRecord(isNegative, years, months, days, intHours, intMinutes, intSeconds, intMilli);
        }

    }

    

    /**
     * Time Zones and associated coordinates handler
     */
    public static final class LocalizationSubClass {
        /** Input time zone variable */
        private static String inputTimeZone;
        /** Output time zone variable */
        private static String outputTimeZone;
        /** Variable used as reference ZoneDateTime for Aging calculation */
        private static ZonedDateTime refAgingTimeStamp;

        static {
            loadTimeZones();
        }

        /**
         * File time related logic
         */
        public static final class FileSubSubClass {

            /**
             * Constructor
             */
            private FileSubSubClass() {
                super();
            }

            /**
             * File Last Modified Aging 
             * @param file given file
             * @return String with file Last Modified aging 
             */
            public static String getFileLastModifiedAging(@NonNull final Path file) {
                final ZonedDateTime zStartTimeStamp = getFileLastModifiedZonedDateTime(file);
                String strReturn = "";
                if (zStartTimeStamp != null) {
                    strReturn = AgingSubClass.computeAgingIntoHumanReadableWords(refAgingTimeStamp, zStartTimeStamp);
                }
                return strReturn;
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
                final ZonedDateTime dateTime = getFileLastModifiedZonedDateTime(file);
                final DateTimeFormatter fixedFormatter = DateTimeFormatter.ofPattern(outFormat, Locale.US);
                String returnString = null;
                if (dateTime != null) {
                    returnString = dateTime.format(fixedFormatter);
                }
                return returnString;
            }

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
        public static String convertDateOrTimestampFormats(@NonNull final String inDate, @NonNull final DateTimeFormatter inTimeFormat, @NonNull final DateTimeFormatter outTimeFormat) {
            String outDate = ""; 
            try {
                ZonedDateTime zonedDateTime = null;
                if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyTimestampWithMilliseconds(inDate)) {
                    zonedDateTime = LocalDateTime.parse(inDate, inTimeFormat).atZone(ZoneId.of(inputTimeZone));
                } else if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyTimestamp(inDate)) {
                    zonedDateTime = LocalDateTime.parse(inDate, inTimeFormat).atZone(ZoneId.of(inputTimeZone));
                } else if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyDate(inDate)) {
                    zonedDateTime = LocalDate.parse(inDate, inTimeFormat).atStartOfDay(ZoneId.of(inputTimeZone));
                }
                if (zonedDateTime == null) {
                    final String strFeedback = String.format("Error parsing %s with following details: %s", inDate, "ZonedDateTime is null");
                    LogExposureClass.LOGGER.error(strFeedback);
                } else {
                    ZonedDateTime outTime = zonedDateTime;
                    if (!inputTimeZone.equalsIgnoreCase(outputTimeZone)) {
                        outTime = zonedDateTime.withZoneSameInstant(ZoneId.of(outputTimeZone));
                    }
                    outDate = outTime.format(outTimeFormat);
                }
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
         * Convert time-stamp
         * @param strTimeStamp input Time-stamp
         * @param inputFormat input Time Format
         * @param outputFormat output Time Format as String
         * @return String converted time-stamp and formatted
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
         * @param strTimeStamp input time-stamp
         * @param inputFormat input Time Format as String
         * @return ZonedDateTime
         */
        private static ZonedDateTime convertStringIntoZonedDateTime(final String strTimeStamp, final String inputFormat) {
            final LocalDateTime localTime = LocalDateTime.parse(strTimeStamp, 
                    DateTimeFormatter.ofPattern(inputFormat, Locale.US));
            return localTime.atZone(ZoneId.of(inputTimeZone));
        }

        /**
         * Convert time-stamp
         * @param inTimeStamp input Time-stamp
         * @param outputFormat output Time Format as String
         * @return String converted time-stamp and formatted
         */
        public static String convertZonedTimestampFriendly(final ZonedDateTime inTimeStamp, final String outputFormat) {
            ZonedDateTime outTime = inTimeStamp;
            if (!inputTimeZone.equalsIgnoreCase(outputTimeZone)) {
                outTime = inTimeStamp.withZoneSameInstant(ZoneId.of(outputTimeZone));
            }
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(outputFormat, Locale.US);
            return outTime.format(formatter);
        }

        /**
         * format date
         * @param strDate input date
         * @param inputFormat input Time Format
         * @param outputFormat output Time Format as String
         * @return String formatted date
         */
        public static String formatDateFriendly(final String strDate, final String inputFormat, final String outputFormat) {
            final LocalDate outDate = LocalDate.parse(strDate, DateTimeFormatter.ofPattern(inputFormat, Locale.US));
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(outputFormat, Locale.US);
            return outDate.format(formatter);
        }

        /**
         * get Last Modified date time
         * @param file given file
         * @return ZonedDateTime
         */
        @Nullable
        public static ZonedDateTime getFileLastModifiedZonedDateTime(@NonNull final Path file) {
            ZonedDateTime zDateTime = null;
            try {
                final Instant modifTime = Files.getLastModifiedTime(file).toInstant();
                zDateTime = ZonedDateTime.ofInstant(modifTime, ZoneId.of(outputTimeZone));
            } catch (IOException ei) {
                final String strFeedback = String.format("Error encountered when attempting to get %s file(s) from %s folder",
                        file.getParent(),
                        file.getFileName());
                LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
            }
            return zDateTime;
        }

        /**
         * loading Input/Output time zones
         */
        private static void loadTimeZones() {
            inputTimeZone = System.getProperty("user.timezone", ZoneId.systemDefault().toString());
            outputTimeZone = System.getProperty("user.timezone", ZoneId.systemDefault().toString());
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
         * Setter for zAgingRefTimeStamp
         * @param inRefTimeStamp input Reference Time-stamp
         */
        public static void setReferenceTimeStampValueForAgingCalculation(final ZonedDateTime inRefTimeStamp) {
            refAgingTimeStamp = inRefTimeStamp;
        }

        /**
         * Constructor
         */
        private LocalizationSubClass() {
            super();
        }

    }

    /**
     * Constructor
     */
    private TimingClass() {
        super();
    }
}
