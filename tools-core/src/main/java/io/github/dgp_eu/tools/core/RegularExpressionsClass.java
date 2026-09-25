/**
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.core;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SequencedMap;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.dgp_eu.tools.core.time.TimingClass;

/**
 * Regular Expressions things
 */
public final class RegularExpressionsClass {
    /** Map for Date/Timestamp/TimestampWithMilliseconds id/validation/conversion */
    private static final Map<String, DateTimeInfoRec> MAP_PATTERNS = new ConcurrentHashMap<>();
    /** Regular Expression for short form Age as Date */
    private static final String REGEXP_AGE_DATE = "[+-]\\d{4}-(0\\d|1[0-1])-([0-2]\\d|30)";
    /** Regular Expression for short form Age as Date exact */
    private static final String REGEXP_AGE_DATE9 = "^" + REGEXP_AGE_DATE + "$";
    /** Regular Expression for short form Age as Time */
    private static final String REGEXP_AGE_TIME = "[+-]([0-1]\\d|2[0-3])\\:[0-5]\\d\\:[0-5]\\d";
    /** Regular Expression for short form Age as Time exact */
    private static final String REGEXP_AGE_TIME9 = "^" + REGEXP_AGE_TIME + "$";
    /** Regular Expression for short form Age as Time-stamp */
    private static final String REGEXP_AGE_TS = "[+-]\\d{4}-(0\\d|1[0-1])-([0-2]\\d|30)\\s([0-1]\\d|2[0-3])\\:[0-5]\\d\\:[0-5]\\d";
    /** Regular Expression for short form Age as Time-stamp full string */
    private static final String REGEXP_AGE_TS9 = "^" + REGEXP_AGE_TS + "$";
    /** Regular Expression for short form Age as Time-stamp with Milliseconds */
    private static final String REGEXP_AGE_TS_MS = "[+-]\\d{4}-(0\\d|1[0-1])-([0-2]\\d|30)\\s([0-1]\\d|2[0-3])\\:[0-5]\\d\\:[0-5]\\d\\.\\d{1,3}";
    /** Regular Expression for short form Age as Time-stamp with Milliseconds fixed */
    private static final String REGEXP_AGE_TS_MS9 = "^" + REGEXP_AGE_TS_MS + "$";
    /** Regular Expression for full words Aging */
    private static final String REGEXP_AGING_FULL = "^(|-)(|\\d{1,6}\\syear(s|)(|\\s))"
            + "(|(1[0-1]|[1-9])\\smonth(s|)(|\\s))"
            + "(|(1\\d|2\\d|30|\\d)\\sday(s|)(|\\s))"
            + "(|(1\\d|2[0-3]|\\d)\\s(hour(s|))(|\\s))"
            + "(|([1-5]\\d|\\d)\\s(minute(s|))(|\\s))"
            + "(|([1-5]\\d|\\d)\\s(second(s|))(|\\s))"
            + "(|\\d{1,3}\\s(millisecond(s|)))$";
    /** Regular Expression for byte size value and unit of measure */
    private static final String REGEXP_BYTE_SIZE = "\\d*\\.?\\d*\\s(byte|bytes|KB|KiB|MB|MiB|GB|GiB|TB|TiB|PB|PiB|EB|EiB)";
    /** Regular Expression for Date identification/validation */
    private static final String REGEXP_DATE = "(1|2)\\d{3}"
            + "\\-("
            + "(01|03|05|07|08|10|12)\\-(0[1-9]|[1-2]\\d|3[0-1])" // months w. 31 days
            + "|(04|06|09|11)\\-(0[1-9]|[1-2]\\d|30)" // month w. 30 days
            + "|02\\-(0[1-9]|[1-2]\\d)" // month w. 28/29 days (February) 
            + ")";
    /** Regular Expression for Latitude */
    private static final String REGEXP_LATITUDE = "([+-])(\\d{2})(\\d{2})(\\d{2})?";
    /** Regular Expression for Longitude */
    private static final String REGEXP_LONGITUDE = "([+-])(\\d{3})(\\d{2})(\\d{2})?";
    /** Regular Expression for Long Time-stamp with Milliseconds  */
    public static final String REGEXP_LONG_TS_MS = "[A-Za-z]{3},\\s\\d{2}\\s[A-Za-z]{3}\\s[1-2]\\d{3}\\s([0-1]\\d|2[0-3]):[0-5]\\d:[0-5]\\d\\.\\d{3}";
    /** Regular Expression for Number as Decimal */
    private static final String REGEXP_MAVEN_PKG = "[0-9a-z]+\\.[0-9a-z\\-\\.]+\\:[0-9a-z\\-\\._]+";
    /** Regular Expression for Number as Decimal */
    private static final String REGEXP_NO_DECIMAL = "-?\\d+\\.\\d+-?";
    /** Regular Expression for Number as Long */
    private static final String REGEXP_NO_LONG = "-?\\d{1,18}-?";
    /** Regular Expression for Number as Numeric */
    private static final String REGEXP_NO_NUMERIC = "-?\\d+(\\.\\d+)?-?";
    /** Regular Expression for Prompt Parameters within SQL Query */
    public static final String REGEXP_PRMTR_RGX = "\\{[0-9A-Za-z_\\s\\-]{2,50}\\}";
    /** Regular Expression for Time identification/validation */
    private static final String REGEXP_TIME = "([0-1]\\d|2[0-3])\\:[0-5]\\d\\:[0-5]\\d";
    /** Regular Expression for Time-stamp identification/validation */
    private static final String REGEXP_TS = REGEXP_DATE + "\\s" + REGEXP_TIME;
    /** Regular Expression for Time-stamp with Milliseconds identification/validation */
    private static final String REGEXP_TS_MS = REGEXP_DATE + "\\s" + REGEXP_TIME + "\\.\\d{3}";
    /** Version Patterns Map */
    private static final String REGEXP_VERSION = "^[0-9.]+(|\\.(Alpha|Beta|CR|Final|RC)|-(alpha|alpha-|Beta|beta|beta-|M|pre1|RC|rc|rc-)\\d{1,2})$";
    /** string constant for agingDate */
    private static final String STR_AGING_DATE = "agingDate";
    /** string constant for agingTime */
    private static final String STR_AGING_TIME = "agingTime";
    /** string constant for agingTimestamp */
    private static final String STR_AGING_TS = "agingTimestamp";
    /** string constant for agingTimestamp */
    private static final String STR_AGING_TS_MS = "agingTimestampWithMilliseconds";
    /** string constant for MavenPackage */
    private static final String STR_MAVEN_PKG = "MavenPackage";
    /** Record for ZoneInfo */
    /* default */ public record DateTimeInfoRec(
        String input,
        String outputLong,
        String outputAbbreviated,
        String regularExpression) {}

    static {
        loadDateTimePatternsIntoMap();
    }

    /**
     * building Central Maven Repository URL
     * @param inPackage input package
     * @return String as URL
     */
    public static String buildCentralMavenRepositoryUniformResourceLocator(final String inPackage) {
        final String[] urlParts = inPackage.split(":");
        return String.format("https://repo1.maven.org/maven2/%s/%s/", urlParts[0].replace('.', '/'), urlParts[1]);
    }

    /**
     * Building Regular Expression patterns
     * @return String of patterns with Regular Expressions
     */
    private static String buildRegExpForMassReplace() {
        final SequencedMap<String, String> sortedRegExp = new LinkedHashMap<>();
        sortedRegExp.put(STR_MAVEN_PKG, REGEXP_MAVEN_PKG);
        sortedRegExp.put(STR_AGING_TS_MS, REGEXP_AGE_TS_MS);
        sortedRegExp.put(STR_AGING_TS, REGEXP_AGE_TS);
        sortedRegExp.put(STR_AGING_DATE, REGEXP_AGE_DATE);
        sortedRegExp.put(STR_AGING_TIME, REGEXP_AGE_TIME);
        sortedRegExp.put(ConfigurationClass.STR_TS_MSEC, "");
        sortedRegExp.put(ConfigurationClass.STR_TIMESTAMP, "");
        sortedRegExp.put(ConfigurationClass.STR_JUST_DATE, "");
        final StringJoiner sjRegExp = new StringJoiner("|");
        sortedRegExp.forEach((key, value) -> {
            if (value.isBlank()) {
                sjRegExp.add(String.format("(?<%s>%s)", key, MAP_PATTERNS.get(key).regularExpression));
            } else {
                sjRegExp.add(String.format("(?<%s>%s)", key, value));
            }
        });
        return sjRegExp.toString();
    }

    /**
     * Count occurrences with String
     * @param haystack string to count in
     * @param needleType type of pattern search
     * @return number of occurrences
     */
    public static int countOccurrences(final String haystack, final String needleType) {
        final Pattern pattern = switch(needleType) {
            case "NamedParameters"                 -> Pattern.compile(REGEXP_PRMTR_RGX);
            case "ComplexPositionalTypeParameters" -> Pattern.compile("%(|[1-9]\\$)(|,\\d{1,3}|\\+|\\(|,)(|\\.[1-9]|\\d{1,2})[abcdefghnostx]");
            case "PositionalTypeParameters"        -> Pattern.compile("%[ACEGHSTXacdefghostx]");
            default                                -> Pattern.compile(".*");
        };
        final Matcher matcher = pattern.matcher(haystack);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * check if pattern exists into text
     * @param text to search within
     * @param pattern to search for
     * @return int 1 if found otherwise 0
     */
    public static int doesExist(final String text, final String pattern) {
        final Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        final Matcher matcher = regex.matcher(text);
        return matcher.find() ? 1 : 0;
    }

    /**
     * Converts Degrees, Minutes, Seconds to Decimal logic
     * @param part degrees+minutes+seconds in
     * @param isLon is Longitude
     * @return double numeric value
     */
    public static double dmsToDecimal(final String part, final boolean isLon) {
        final Pattern inPattern = Pattern.compile(isLon ? REGEXP_LONGITUDE : REGEXP_LATITUDE);
        final Matcher matched = inPattern.matcher(part);
        double decToReturn = 0.0;
        if (matched.matches()) {
            final double sign = "-".equals(matched.group(1)) ? -1.0 : 1.0;
            final double deg = BasicStructuresClass.convertStringIntoDouble(matched.group(2));
            final double min = BasicStructuresClass.convertStringIntoDouble(matched.group(3));
            final double sec = (matched.group(4) != null) ? BasicStructuresClass.convertStringIntoDouble(matched.group(4)) : 0.0;
            decToReturn = sign * (deg + (min / 60.0) + (sec / 3600.0));
        }
        return decToReturn;
    }

    /**
     * Extracts all occurrences of a given regex pattern from a text.
     * @param text The input string to search within.
     * @param regex The regular expression pattern.
     * @return A List of strings, where each string is a full match found.
     */
    public static List<String> extractMatches(final String text, final String regex) {
        final List<String> matches = new ArrayList<>();
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            matches.add(matcher.group()); // group() or group(0) returns the entire matched sequence
        }
        return matches;
    }

    /**
     * Helper to find which named group was actually hit by the regex
     * @param result match result group
     * @return name of the active group
     */
    public static String getActiveGroup(final MatchResult result) {
        final List<String> capturedGroups = List.of(
                STR_MAVEN_PKG,
                STR_AGING_TS,
                STR_AGING_TS_MS,
                STR_AGING_DATE,
                ConfigurationClass.STR_TS_MSEC,
                ConfigurationClass.STR_TIMESTAMP,
                ConfigurationClass.STR_JUST_DATE);
        return capturedGroups.stream()
                .filter(groupName -> result.group(groupName) != null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No group matched"));
    }

    /**
     * Getter for MAP_PATTERNS
     * @return Map for Time/Date patterns formats and regular expressions
     */
    public static Map<String, DateTimeInfoRec> getMapPatterns() {
        return MAP_PATTERNS;
    }

    /**
     * Populates MAP_PATTERNS list
     */
    private static void loadDateTimePatternsIntoMap() {
        MAP_PATTERNS.put(ConfigurationClass.STR_TS_MSEC,
                new DateTimeInfoRec(TimingClass.DATE_TIME_MS, TimingClass.DATE_TIME_MS_LONG, TimingClass.DATE_TIME_MS_ABRV, REGEXP_TS_MS));
        MAP_PATTERNS.put(ConfigurationClass.STR_TIMESTAMP,
                new DateTimeInfoRec(TimingClass.DATE_TIME, TimingClass.DATE_TIME_LONG, TimingClass.DATE_TIME_ABRV, REGEXP_TS));
        MAP_PATTERNS.put(ConfigurationClass.STR_JUST_DATE,
                new DateTimeInfoRec(TimingClass.ISO_DATE, TimingClass.ISO_DATE_LONG, TimingClass.ISO_DATE_ABRV, REGEXP_DATE));
    }

    /**
     * Replace patterns within large Text
     * @param inString original text
     * @return replaced text
     */
    public static String replacePatternsWithTimeZones(final String inString) {
        final String strRegExp = buildRegExpForMassReplace();
        final Pattern pattern = Pattern.compile(strRegExp);
        final Matcher matcher = pattern.matcher(inString);
        return matcher.replaceAll(matchResult -> {
            try {
                // Determine which group matched
                final String matchedGroup = getActiveGroup(matchResult);
                final String text = matchResult.group(matchedGroup);
                switch (matchedGroup) {
                    case STR_MAVEN_PKG -> {
                        if (text.contains("compliance-snowflake")) {
                            return text;
                        } else {
                            final String strURL = buildCentralMavenRepositoryUniformResourceLocator(text);
                            return String.format("<a href=\"%s\" target=\"_blank\">%s</a>", strURL, text);
                        }
                    }
                    case STR_AGING_TS_MS, STR_AGING_TS, STR_AGING_TIME, STR_AGING_DATE -> {
                        final TimingClass.AgingInfoRecord ageComponents = ConversionSubClass.convertAgingTimestampStringIntoAgingComponents(text);
                        String strZeroValue = "INSTANT (less than 1 millisecond)";
                        if (STR_AGING_DATE.equalsIgnoreCase(matchedGroup)) {
                            strZeroValue = "TODAY";
                        }
                        return TimingClass.AgingSubClass.composeAgingInWordsFromListOfIntegerComponents(ageComponents, strZeroValue);
                    }
                    default -> {
                        final String inPattern = MAP_PATTERNS.get(matchedGroup).input;
                        final DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern(inPattern, Locale.US);
                        final String outPattern = MAP_PATTERNS.get(matchedGroup).outputAbbreviated;
                        final DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern(outPattern, Locale.US);
                        return TimingClass.LocalizationSubClass.convertDateOrTimestampFormats(text, inputFormat, outputFormat);
                    }
                }
            } catch (IllegalStateException _) {
                return matchResult.group(); // Fallback if parsing fails
            }
        });
    }

    /**
     * Conversion logic using Regular Expressions
     */
    public static final class ConversionSubClass {

        /**
         * convert Aging String into Aging Components
         * @param inString input String to convert
         * @return AgingInfoRecord with components
         */
        public static TimingClass.AgingInfoRecord convertAgingTimestampStringIntoAgingComponents(final String inString) {
            final int strLength         = inString.length();
            final String agingRegExp    = establishRelevantRegularExpression(inString, strLength);
            final Pattern pattern       = Pattern.compile(agingRegExp);
            final boolean isAgingString = pattern.matcher(inString).matches();
            if (isAgingString) {
                int years      = 0;
                int months     = 0;
                int days       = 0;
                int intHours   = 0;
                int intMinutes = 0;
                int intSeconds = 0;
                int milli      = 0;
                switch (strLength) {
                    case 9:
                        intHours   = Integer.parseInt(inString.substring(1, 3));
                        intMinutes = Integer.parseInt(inString.substring(4, 6));
                        intSeconds = Integer.parseInt(inString.substring(7, 9));
                        break;
                    case 11:
                        years      = Integer.parseInt(inString.substring(1, 5));
                        months     = Integer.parseInt(inString.substring(6, 8));
                        days       = Integer.parseInt(inString.substring(9, 11));
                        break;
                    case 20, 24:
                        years      = Integer.parseInt(inString.substring(1, 5));
                        months     = Integer.parseInt(inString.substring(6, 8));
                        days       = Integer.parseInt(inString.substring(9, 11));
                        intHours   = Integer.parseInt(inString.substring(12, 14));
                        intMinutes = Integer.parseInt(inString.substring(15, 17));
                        intSeconds = Integer.parseInt(inString.substring(18, 20));
                        milli      = strLength == 24 ? Integer.parseInt(inString.substring(21, 24)) : 0;
                        break;
                    default:
                        // intentionally blank
                        break;
                }
                return new TimingClass.AgingInfoRecord(false, years, months, days, intHours, intMinutes, intSeconds, milli);
            } else {
                final String strFeedbackErr = String.format("Given input String %s does not seem to be an Aging Timestamp String... %s",
                        inString,
                        StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                throw new UnsupportedOperationException(strFeedbackErr);
            }
        }

        /**
         * Establish RegExp for Aging conversion
         * @param inString input String to convert
         * @param inputLength length of input String
         * @return  String with relevant RegExp
         */
        private static String establishRelevantRegularExpression(final String inString, final int inputLength) {
            return switch (inputLength) {
                case 9  -> REGEXP_AGE_TIME9;
                case 11 -> REGEXP_AGE_DATE9;
                case 20 -> REGEXP_AGE_TS9;
                case 24 -> REGEXP_AGE_TS_MS9;
                default -> {
                    final String strFeedbackErr = String.format("An Aging Timestamp String is expected to have either 20 or 24 characters but given one %s has %s number of characters... %s",
                        inString,
                        inputLength,
                        StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                    throw new UnsupportedOperationException(strFeedbackErr);
                }
            };
        }

        // Private constructor to prevent instantiation
        private ConversionSubClass() {
            // intentionally blank
        }

    }

    /**
     * Validation logic using Regular Expressions
     */
    public static final class ValidationSubClass {
        /** Variable for Regular Expression patterns */
        private static final Properties REG_EXP_PROPS = new Properties();

        static {
            loadRegularExpressionProperties();
        }

        /**
         * Check if String is actually Date
         *
         * @param inputString string to evaluate
         * @return True if given String is actually Date
         */
        public static boolean isStringActuallySomething(final String inputString, final String mapIdentifier) {
            boolean bolReturn = false;
            if (inputString != null) {
                final String regularExpression = REG_EXP_PROPS.containsKey(mapIdentifier)
                        ? REG_EXP_PROPS.getProperty(mapIdentifier)
                                : MAP_PATTERNS.get(mapIdentifier).regularExpression;
                final Pattern pattern = Pattern.compile(regularExpression, Pattern.CASE_INSENSITIVE);
                bolReturn = pattern.matcher(inputString).matches();
            }
            return bolReturn;
        }

        /**
         * Validation file name
         * @param value given file name
         * @return true if file name is valid, false otherwise
         */
        public static boolean isFileNameValid(final String value) {
            boolean validFileName = true;
            if (value == null || value.isBlank()) {
                final String strFeedback = "File name must not be null or blank";
                LogExposureClass.LOGGER.error(strFeedback);
                validFileName = false;
            } else if(!value.matches("^[a-zA-Z0-9](?:[a-zA-Z0-9 ._-]*[a-zA-Z0-9])?\\.[a-zA-Z0-9_-]+$")) {
                final String strFeedback = "File name contains invalid characters";
                LogExposureClass.LOGGER.error(strFeedback);
                validFileName = false;
            }
            return validFileName;
        }

        /**
         * loading Regular Expression Properties 
         */
        private static void loadRegularExpressionProperties() {
            REG_EXP_PROPS.put("byteSize", REGEXP_BYTE_SIZE);
            REG_EXP_PROPS.put("decimal", REGEXP_NO_DECIMAL);
            REG_EXP_PROPS.put("fullAging", REGEXP_AGING_FULL);
            REG_EXP_PROPS.put("integer", REGEXP_NO_LONG);
            REG_EXP_PROPS.put("long", REGEXP_NO_LONG);
            REG_EXP_PROPS.put("numeric", REGEXP_NO_NUMERIC);
            REG_EXP_PROPS.put(REGEXP_LONG_TS_MS, REGEXP_LONG_TS_MS);
            REG_EXP_PROPS.put(STR_AGING_TS_MS, REGEXP_AGE_TS_MS9);
            REG_EXP_PROPS.put(STR_AGING_TS, REGEXP_AGE_TS9);
            REG_EXP_PROPS.put(STR_AGING_TIME, REGEXP_AGE_TIME9);
            REG_EXP_PROPS.put(STR_AGING_DATE, REGEXP_AGE_DATE9);
            REG_EXP_PROPS.put(ConfigurationClass.STR_TIMESTAMP, REGEXP_TS);
            REG_EXP_PROPS.put(ConfigurationClass.STR_TS_MSEC, REGEXP_TS_MS);
            REG_EXP_PROPS.put(ConfigurationClass.STR_JUST_DATE, REGEXP_DATE);
            REG_EXP_PROPS.put("version", REGEXP_VERSION);
        }

        // Private constructor to prevent instantiation
        private ValidationSubClass() {
            // intentionally blank
        }

    }

    /**
     * Constructor
     */
    private RegularExpressionsClass() {
        // intentionally blank
    }

}
