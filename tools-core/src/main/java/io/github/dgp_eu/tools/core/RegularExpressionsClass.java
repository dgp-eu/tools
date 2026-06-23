/**
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.core;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regular Expressions things
 */
public final class RegularExpressionsClass {
    /** Patterns Map */
    public static final Map<String, Map<String, String>> MAP_PATTERNS = Map.of(
            RegularExpressionsClass.STR_AGING_DATE, Map.of(RegularExpressionsClass.STR_REG_EXP, "[+-](?<years>\\d{4})-(?<months>(0\\d{1}|1[0-1]{1}))-(?<days>([0-2]{1}\\d{1}|30))"),
            RegularExpressionsClass.STR_AGING_TS, Map.of(RegularExpressionsClass.STR_REG_EXP, "[+-](?<yearsTS>\\d{4})-(?<monthsTS>(0\\d{1}|1[0-1]{1}))-(?<daysTS>([0-2]{1}\\d{1}|30))\\s(?<hoursTS>([0-1]\\d{1}|2[0-3]{1}))\\:(?<minutesTS>[0-5]{1}\\d{1})\\:(?<secondsTS>[0-5]{1}\\d{1})"),
            RegularExpressionsClass.STR_AGING_TIME, Map.of(RegularExpressionsClass.STR_REG_EXP, "(?<hours>([0-1]\\d{1}|2[0-3]{1}))\\:(?<minutes>[0-5]{1}\\d{1})\\:(?<seconds>[0-5]{1}\\d{1})"),
            "decimal", Map.of(RegularExpressionsClass.STR_REG_EXP, "-?\\d+\\.\\d+-?"),
            BasicStructuresClass.STR_JUST_DATE, Map.of(BasicStructuresClass.STR_INPUT, "yyyy-MM-dd",
                    BasicStructuresClass.STR_OUTPUT_LONG, "EEEE, dd MMMM yyyy",
                    BasicStructuresClass.STR_OUTPUT_SHORT, "EEE, dd MMM yyyy",
                    RegularExpressionsClass.STR_REG_EXP, "(1|2)\\d{3}\\-((01|03|05|07|08|10|12)\\-(0{1}[1-9]{1}|[1-2]{1}\\d{1}|3[0-1]{1})|(04|06|09|11)\\-(0{1}[1-9]{1}|[1-2]{1}\\d{1}|30)|02\\-(0[1-9]{1}|[1-2]{1}\\d{1}))"),
            "long", Map.of(RegularExpressionsClass.STR_REG_EXP, "-?\\d{1,18}-?"),
            RegularExpressionsClass.STR_MAVEN_PKG, Map.of(RegularExpressionsClass.STR_REG_EXP, "[0-9a-z]+\\.[0-9a-z\\-\\.]+\\:[0-9a-z\\-\\.]+",
                    "URL", "https://repo1.maven.org/maven2/%s/%s/",
                    "HTMLlink", "<a href=\"%s\" target=\"_blank\">%s</a>"),
            "numeric", Map.of(RegularExpressionsClass.STR_REG_EXP, "-?\\d+(\\.\\d+)?-?"),
            BasicStructuresClass.STR_TIMESTAMP, Map.of(BasicStructuresClass.STR_INPUT, "yyyy-MM-dd HH:mm:ss",
                    BasicStructuresClass.STR_OUTPUT_LONG, "EEEE, dd MMMM yyyy HH:mm:ss",
                    BasicStructuresClass.STR_OUTPUT_SHORT, "EEE, dd MMM yyyy HH:mm:ss",
                    RegularExpressionsClass.STR_REG_EXP, "(1|2)\\d{3}\\-((01|03|05|07|08|10|12)\\-(0{1}[1-9]{1}|[1-2]{1}\\d{1}|3[0-1]{1})|(04|06|09|11)\\-(0{1}[1-9]{1}|[1-2]{1}\\d{1}|30)|02\\-(0[1-9]{1}|[1-2]{1}[0-9]{1}))\\s([0-1]\\d{1}|2[0-3]{1})\\:[0-5]{1}\\d{1}\\:[0-5]{1}\\d{1}"),
            BasicStructuresClass.STR_TS_MSEC, Map.of(BasicStructuresClass.STR_INPUT, "yyyy-MM-dd HH:mm:ss.SSS",
                    BasicStructuresClass.STR_OUTPUT_LONG, "EEEE, dd MMMM yyyy HH:mm:ss.SSS",
                    BasicStructuresClass.STR_OUTPUT_SHORT, "EEE, dd MMM yyyy HH:mm:ss.SSS",
                    RegularExpressionsClass.STR_REG_EXP, "(1|2)\\d{3}\\-((01|03|05|07|08|10|12)\\-(0{1}[1-9]{1}|[1-2]{1}\\d{1}|3[0-1]{1})|(04|06|09|11)\\-(0{1}[1-9]{1}|[1-2]{1}\\d{1}|30)|02\\-(0[1-9]{1}|[1-2]{1}[0-9]{1}))\\s([0-1]\\d{1}|2[0-3]{1})\\:[0-5]{1}\\d{1}\\:[0-5]{1}\\d{1}\\.\\d{3}")
            );
    /** Version Patterns Map */
    public static final String REGEXP_VERSION = "^[0-9.]+(|\\.(Alpha|Beta|CR|Final|RC)|-(alpha|alpha-|Beta|beta|beta-|M|pre1|RC|rc|rc-)\\d{1,2})$";
    /** Regular Expression for Prompt Parameters within SQL Query */
    public static final String STR_PRMTR_RGX = "\\{[0-9A-Za-z_\\s\\-]{2,50}\\}";
    /** Regular Expression for Long Timestamp with Milliseconds  */
    public static final String STR_LONG_TS_MSEC = "[A-Za-z]{3},\\s\\d{2}\\s[A-Za-z]{3}\\s[1-2]\\d{3}\\s([0-1]\\d|2[0-3]):[0-5]\\d:[0-5]\\d\\.\\d{3}";

    /**
     * Regex for Latitude: Sign, 2 digits (deg), 2 digits (min), 
     * optional 2 digits (sec)
     */
    private static final Pattern LAT_REGEX = Pattern.compile("([+-])(\\d{2})(\\d{2})(\\d{2})?");
    /**
     * Regex for Longitude: Sign, 3 digits (deg), 2 digits (min), 
     * optional 2 digits (sec)
     */
    private static final Pattern LON_REGEX = Pattern.compile("([+-])(\\d{3})(\\d{2})(\\d{2})?");
    /**
     * Regular Expression string
     */
    public static final String STR_REG_EXP = "Regular Expression";
    /**
     * string constant
     */
    public static final String STR_AGING_DATE = "agingDate";
    /**
     * string constant
     */
    public static final String STR_AGING_TIME = "agingTime";
    /**
     * string constant
     */
    public static final String STR_AGING_TS = "agingTimestamp";
    /**
     * string constant
     */
    public static final String STR_MAVEN_PKG = "MavenPackage";

    /**
     * building Central Maven Repository URL
     * @param inPackage input package
     * @return String as URL
     */
    public static String buildCentralMavenRepositoryUniformResourceLocator(final String inPackage) {
        final String[] urlParts = inPackage.split(":");
        return String.format(MAP_PATTERNS.get(STR_MAVEN_PKG).get("URL"), urlParts[0].replace('.', '/'), urlParts[1]);
    }

    /**
     * Building Regular Expression patterns
     * @return String of patterns with Regular Expressions
     */
    private static String buildRegExpForMassReplace() {
        final SequencedMap<String, String> sortedRegExp = new LinkedHashMap<>();
        sortedRegExp.put(STR_MAVEN_PKG, "");
        sortedRegExp.put(STR_AGING_TS, "");
        sortedRegExp.put(STR_AGING_DATE, "");
        sortedRegExp.put(BasicStructuresClass.STR_TS_MSEC, "");
        sortedRegExp.put(BasicStructuresClass.STR_TIMESTAMP, "");
        sortedRegExp.put(BasicStructuresClass.STR_JUST_DATE, "key");
        final StringJoiner sjRegExp = new StringJoiner("|");
        sortedRegExp.forEach((key, _) -> sjRegExp.add(String.format("(?<%s>%s)", key, MAP_PATTERNS.get(key).get(STR_REG_EXP))));
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
            case "NamedParameters" -> Pattern.compile(STR_PRMTR_RGX);
            case "ComplexPositionalTypeParameters" -> Pattern.compile("%(|[1-9]\\$)(|,\\d{1,3}|\\+|\\(|,)(|\\.[1-9]|\\d{1,2})[abcdefghnostx]");
            case "PositionalTypeParameters" -> Pattern.compile("%[ACEGHSTXacdefghostx]");
            default -> Pattern.compile(".*");
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
     * @return int
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
        final Matcher matched = (isLon ? LON_REGEX : LAT_REGEX).matcher(part);
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
                STR_AGING_DATE,
                BasicStructuresClass.STR_TS_MSEC,
                BasicStructuresClass.STR_TIMESTAMP,
                BasicStructuresClass.STR_JUST_DATE);
        return capturedGroups.stream()
                .filter(groupName -> result.group(groupName) != null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No group matched"));
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
                            return String.format(MAP_PATTERNS.get(STR_MAVEN_PKG).get("HTMLlink"), strURL, text);
                        }
                    }
                    case STR_AGING_TS -> {
                        final String strDate = text.substring(0, 11);
                        final String strTime = text.substring(12, 20);
                        return ConversionSubClass.convertAgingDateIntoHumanReadableString(strDate)
                                + "<br/>" + ConversionSubClass.convertAgingTimeIntoHumanReadableString(strTime);
                    }
                    case STR_AGING_DATE -> {
                        final String outString = ConversionSubClass.convertAgingDateIntoHumanReadableString(text);
                        return outString.isEmpty() ? "TODAY" : outString;
                    }
                    default -> {
                        return TimingClass.ConversionSubClass.convertTimeFormat(text,
                                MAP_PATTERNS.get(matchedGroup).get(BasicStructuresClass.STR_INPUT),
                                MAP_PATTERNS.get(matchedGroup).get(BasicStructuresClass.STR_OUTPUT_SHORT));
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
         * Convert aging Date/Time into human-readable String
         * @param ageString input String
         * @return String in human-readable format
         */
        public static String convertAgingDateOrTime(final Matcher matcher, final SequencedMap<String, String> seqMapDateTime, final String ageString) {
            final List<String> resultDateOrTime = new ArrayList<>();
            if (matcher.matches()) {
                seqMapDateTime.forEach((strPlural, strSingular) -> {
                    try {
                        final int intValue = Integer.parseInt(matcher.group(strPlural));
                        if (intValue != 0) {
                            resultDateOrTime.add(numberWithSuffixIfNonZero(intValue, strSingular, strPlural));
                        }
                    } catch (NumberFormatException noFormatException) {
                        final String strFeedback = String.format(BasicStructuresClass.CONVERT_INT_NA, strPlural, Arrays.toString(noFormatException.getStackTrace()));
                        LogExposureClass.LOGGER.error(strFeedback);
                    }
                });
            } else {
                resultDateOrTime.add(ageString);
            }
            return resultDateOrTime.toString().replaceAll("[\\[\\]]", "");
        }

        /**
         * Convert aging Date into human-readable String
         * @param ageString input String
         * @return String in human-readable format
         */
        public static String convertAgingDateIntoHumanReadableString(final String ageString) {
            final Pattern agePattern = Pattern.compile(MAP_PATTERNS.get(STR_AGING_DATE).get(STR_REG_EXP));
            final Matcher matcher = agePattern.matcher(ageString);
            final SequencedMap<String, String> sequencedMapDate = new LinkedHashMap<>();
            sequencedMapDate.put("years", "year");
            sequencedMapDate.put("months", "month");
            sequencedMapDate.put("days", "day");
            return convertAgingDateOrTime(matcher, sequencedMapDate, ageString);
        }

        /**
         * Convert aging Time into human-readable String
         * @param ageString input String
         * @return String in human-readable format
         */
        public static String convertAgingTimeIntoHumanReadableString(final String ageString) {
            final Pattern agePattern = Pattern.compile(MAP_PATTERNS.get(STR_AGING_TIME).get(STR_REG_EXP));
            final Matcher matcher = agePattern.matcher(ageString);
            final SequencedMap<String, String> sequencedMapTime = new LinkedHashMap<>();
            sequencedMapTime.put("hours", "hour");
            sequencedMapTime.put("minutes", "minute");
            sequencedMapTime.put("seconds", "second");
            return convertAgingDateOrTime(matcher, sequencedMapTime, ageString);
        }

        /**
         * Number with Suffix If Non-Zero
         * @param inNumber number to evaluate
         * @param strSingular singular suffix
         * @param strPlural plural suffix
         * @return number with suffix or empty if number is zero
         */
        private static String numberWithSuffixIfNonZero(final int inNumber, final String strSingular, final String strPlural) {
            return switch(inNumber) {
                case 0  -> "";
                case 1  -> inNumber + " " + strSingular;
                default -> inNumber + " " + strPlural;
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

        /**
         * Check if String is actually Date
         *
         * @param inputString string to evaluate
         * @return True if given String is actually Date
         */
        public static boolean isStringActuallySomething(final String inputString, final String mapIdentifier) {
            boolean bolReturn = false;
            if (inputString != null) {
                String regularExpression = REGEXP_VERSION;
                if (STR_LONG_TS_MSEC.equals(mapIdentifier)) {
                    regularExpression = STR_LONG_TS_MSEC;
                } else if (!"version".equalsIgnoreCase(mapIdentifier)) {
                    regularExpression = MAP_PATTERNS.get(mapIdentifier).get(STR_REG_EXP);
                }
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
