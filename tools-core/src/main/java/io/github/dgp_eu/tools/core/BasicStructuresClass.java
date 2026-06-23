/**
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SequencedMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Handling basic structures: numbers, lists, maps, strings
 */
public final class BasicStructuresClass {
    /** Constant for non or single/one */
	/* default */ public static final String ARITY_NONE_OR_ONE = "0..1";
    /** arity one or more */
    /* default */ public static final String ARITY_ONE_OR_MORE = "1..*";
    /** One as string */
    /* default */ public static final String ARITY_ONLY_ONE = "1";
    /** default Locale */
    public static final String DEFAULT_LOCALE = "en-US";
    /** "Active Pixels" constant */
    public static final String STR_ACTV_PXLS = "Active Pixels";
    /** Content constant */
    public static final String STR_CONTENT = "Content";
    /** frequently expression used to catch conversion error  */
    public static final String CONVERT_INT_NA = "Could not convert value %s into Integer... %s";
    /** Dependencies constant */
    public static final String STR_DEPENDENCIES = "Dependencies";
    /** String for internal ETL */
    public static final String STR_DOT_THREE = "DotAndThreeDigitNumber";
    /** String for internal Environment */
    public static final String STR_ENV = "Environment";
    /** String for internal Environment Details */
    public static final String STR_ENV_DTLS = "EnvironmentDetails";
    /** String for internal Executing query successful */
    public static final String STR_EXEC_QRY_OK = "Executing %s query was successful!";
    /** Constant for File Hashing */
    public static final String STR_FILE_HASHING = "FileHashing";
    /** Firmware string */
    public static final String STR_FIRMWARE = "Firmware";
    /** HumanReadableTime constant */
    public static final String STR_TM_HUMAN = "HumanReadableTime";
    /** Icon string */
    public static final String STR_ICON = "icon";
    /** Index string */
    public static final String STR_INDEX = "index";
    /** Input string */
    public static final String STR_INPUT = "Input";
    /** Just Date string */
    public static final String STR_JUST_DATE = "justDate";
    /** Locale constant */
    public static final String STR_LOCALE = "Locale";
    /** Localization constant */
    public static final String STR_LOCALIZATION = "Localization";
    /** Mainboard constant */
    public static final String STR_MAINBOARD = "Mainboard";
    /** Manufacturer string */
    public static final String STR_MANUFACTURER = "Manufacturer";
    /** Menu string */
    public static final String STR_MENU = "menu";
    /** Model string constant */
    public static final String STR_MODEL = "Model";
    /** string constant */
    public static final String STR_MONITOR_NAME = "Monitor Name";
    /** Multiple constant */
    public static final String STR_MULTIPLE = "multiple";
    /** Name constant */
    public static final String STR_NAME = "Name";
    /** NULL constant */
    public static final String STR_NULL = "NULL";
    /** NamedParameter constant */
    public static final String STR_NAMED_PARAM = "NamedParameter";
    /** new tab and table feature */
    public static final String STR_NEW_TAB = "New Tab and Table on column value change";
    /** Output Long constant */
    public static final String STR_OUTPUT_LONG = "Output Long";
    /** Output Short constant */
    public static final String STR_OUTPUT_SHORT = "Output Short";
    /** "Physical Dimensions" constant */
    public static final String STR_PHYSC_DIM = "Physical Dimensions";
    /** "Preferred Timing Clock" constant */
    public static final String STR_PRFRD_TM_CLCK = "Preferred Timing Clock";
    /** "Range Limits" constant */
    public static final String STR_RANGE_LMTS = "Range Limits";
    /** "RowStyle" constant */
    public static final String STR_ROW_STYLE = "RowStyle";
    /** String for Second */
    public static final String STR_SECOND = "Second";
    /** String for internal ETL */
    public static final String STR_SLMN_TWO = "SemicolumnAndTwoDigitNumber";
    /** Database Snowflake */
    public static final String STR_SNOWFLAKE = "Snowflake";
    /** Constant for Software Releases */
    public static final String STR_SOFTWARE_RLS = "SoftwareReleases";
    /** "Serial Number" constant */
    public static final String STR_SRL_NUM = "Serial Number";
    /** Database SQLite */
    public static final String STR_SQLITE = "SQLite";
    /** internal rule constant for timing computation */
    public static final String STR_TM_FRM_SP = "SpaceTwoDigitNumberAndSpaceAndSuffixOnlyIfGreaterThanZero";
    /** System constant */
    public static final String STR_SYSTEM = "System";
    /** Style constant */
    public static final String STR_STYLE = "style";
    /** Table constant */
    public static final String STR_TABLE = "Table";
    /** Table Statistics constant */
    public static final String STR_TS = "TableStatistics";
    /** Time-stamp constant */
    public static final String STR_TIMESTAMP = "timestamp";
    /** Time-stamp constant */
    public static final String STR_TS_MSEC = "timestampWithMilliseconds";
    /** Title constant */
    public static final String STR_TITLE = "title";
    /** String for internal ETL */
    public static final String STR_TWO = "TwoDigitNumber";
    /** String for internal ETL */
    public static final String STR_TWO_NON_ZERO = "TwoDigitNumberOnlyIfGreaterThanZero";
    /** Vendor string */
    public static final String STR_VENDOR = "Vendor";
    /** Version string */
    public static final String STR_VERSION = "Version";
    /** Yes string */
    public static final String STR_YES = "YES";

    /**
     * Safely computes percentage
     * @param numerator top number
     * @param denominator dividing number
     * @return float value
     */
    public static float computePercentageSafely(final long numerator, final long denominator) {
        long denominatorUsed = denominator;
        if (denominatorUsed == 0) {
            denominatorUsed = 100;
            final String strFeedback = String.format("Denominator is 0 hence Percentage calculation with Numerator %s is not possible and will return same numerator...", numerator);
            LogExposureClass.LOGGER.error(strFeedback);
        }
        final double percentageExact = (float) numerator * 100 / denominatorUsed;
        return (float) new BigDecimal(Double.toString(percentageExact))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Convert String to BigDecimal
     * @param strNumber string to evaluate
     * @return BigDecimal
     */
    public static BigDecimal convertStringIntoBigDecimal(final String strNumber) {
        BigDecimal noToReturn = null;
        final boolean isNumeric = StringEvaluationSubClass.isStringActuallyNumeric(strNumber);
        if (isNumeric) {
            noToReturn = new BigDecimal(strNumber).stripTrailingZeros();
        }
        return noToReturn;
    }

    /**
     * Convert String to Double
     * @param strNumber string to evaluate
     * @return double
     */
    public static double convertStringIntoDouble(final String strNumber) {
        double noToReturn = 0.0;
        try {
            noToReturn = Double.parseDouble(strNumber);
        } catch (NumberFormatException noFormatException) {
            final String strFeedback = String.format("Could not convert value %s into Double... %s", strNumber,
                    Arrays.toString(noFormatException.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return noToReturn;
    }

    /**
     * Convert String to Integer
     * @param strNumber string to evaluate
     * @return integer
     */
    public static int convertStringIntoInteger(final String strNumber) {
        int noToReturn = 0;
        final boolean isNumeric = StringEvaluationSubClass.isStringActuallyInteger(strNumber);
        if (isNumeric) {
            try {
                noToReturn = Integer.parseInt(strNumber);
            } catch (NumberFormatException noFormatException) {
                final String strFeedback = String.format(CONVERT_INT_NA, strNumber,
                        Arrays.toString(noFormatException.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }
        return noToReturn;
    }

    /**
     * Convert String to Integer
     * @param strNumber string to evaluate
     * @return long
     */
    public static long convertStringIntoLong(final String strNumber) {
        long noToReturn = 0;
        final boolean isNumeric = StringEvaluationSubClass.isStringActuallyLong(strNumber);
        if (isNumeric) {
            try {
                noToReturn = Long.parseLong(strNumber);
            } catch (NumberFormatException noFormatException) {
                final String strFeedback = String.format(CONVERT_INT_NA, strNumber,
                        Arrays.toString(noFormatException.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }
        return noToReturn;
    }

    /**
     * Extracts all occurrences of a given reg-ex pattern from a text.
     * @param inputString The input string to search within.
     * @return A List of strings, where each string is a full match found.
     */
    public static int countNamedParametersWithinQuery(final String inputString) {
        return RegularExpressionsClass.countOccurrences(inputString, "NamedParameters");
    }

    /**
     * Counts number of parameters with in a string
     * @param inputString string to evaluate
     * @return number of parameters within given string
     */
    public static int countPositionalTypeParametersWithinQuery(final String inputString) {
        return RegularExpressionsClass.countOccurrences(inputString, "PositionalTypeParameters");
    }

    /**
     * Getting current project folder
     * @return application folder
     */
    public static String getCurrentFolder() {
        String strAppFolder = "";
        final File directory = new File(""); // parameter is empty
        try {
            strAppFolder = directory.getCanonicalPath();
        } catch (IOException ex) {
            final String strFeedback = String.format("Error encountered in getting folder... %s", Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return strAppFolder;
    }

    /**
     * detects if current execution is from JAR or not
     * @return boolean
     */
    public static boolean isRunningFromJar() {
        // Get the URL of the current class's byte-code
        final URL classUrl = ProjectClass.class.getResource("BasicStructuresClass.class");
        if (classUrl == null) {
            throw new IllegalStateException("Class resource not found");
        }
        // Check if the protocol is "jar" (JAR execution) or "file" (IDE execution)
        final String protocol = classUrl.getProtocol();
        return "jar".equals(protocol);
    }

    /**
     * List and Maps management
     */
    public static final class ListAndMapSubClass {

        /**
         * Map into List
         * @param strCategory value for Category Property
         * @param inMap values as Map
         * @return List of Properties
         */
        public static List<Properties> convertMapOfStringsIntoListOfProperties(final String strCategory, final Map<String, Object> inMap) {
            final List<Properties> resultReleases = new ArrayList<>();
            inMap.forEach((strKey, strValue) -> {
                final Properties mProperties = new Properties();
                mProperties.put("Category", strCategory);
                mProperties.put("Element", strKey);
                mProperties.put("Value", strValue);
                resultReleases.add(mProperties);
            });
            resultReleases.sort(Comparator.comparing(p -> p.getProperty("Element")));
            return resultReleases;
        }

        /**
         * Get all words from a list of Strings with merged words glued by _
         * @param valList List of String with words glues by regexSep
         * @param regexSep separators for words detection
         * @return LinkedHashMap of Strings with counted occurrences
         */
        public static SequencedMap<String, Long> getWordCounts(final List<String> valList, final String regexSep) {
            final Map<String, Long> wordCounts = valList.stream()
                    .flatMap(s -> Arrays.stream(s.split(regexSep)))
                    .collect(Collectors.groupingBy(
                            word -> word,
                            Collectors.counting()
                    ));
            return wordCounts.entrySet().stream()
                    .sorted(
                            Comparator.comparing(Map.Entry<String, Long>::getValue).reversed()
                                    .thenComparing(Map.Entry::getKey)
                    )
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, _) -> e1, // merge function (not used here)
                            LinkedHashMap::new // preserve sorted order
                    ));
        }

        /**
         * Merging keys based on list of rules
         * @param inputMap original map
         * @param mergeRules merging rules
         * @return Map of string list
         */
        public static Map<String, List<String>> mergeKeys(
                final Map<String, List<String>> inputMap,
                final Map<List<String>, String> mergeRules) {
            final Map<String, List<String>> result = new ConcurrentHashMap<>();
            // Keep track of all keys that will be merged (to exclude later)
            final Set<String> mergedKeys = new HashSet<>();
            final List<String> mergedValues = new ArrayList<>();
            // Apply each merge rule
            for (final Map.Entry<List<String>, String> entry : mergeRules.entrySet()) {
                final List<String> keysToMerge = entry.getKey();
                for (final String key : keysToMerge) {
                    mergedValues.addAll(inputMap.getOrDefault(key, List.of()));
                }
                if (!mergedValues.isEmpty()) {
                    result.put(entry.getValue(), mergedValues);
                }
                mergedKeys.addAll(keysToMerge);
                mergedValues.clear();
            }
            // Copy all keys that were not merged
            for (final Map.Entry<String, List<String>> entry : inputMap.entrySet()) {
                if (!mergedKeys.contains(entry.getKey())) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        }

        /**
         * produce a Sequenced Map from Properties
         * @param prop Properties
         * @param order order as List of String
         * @return SequencedMap with sorted properties
         */
        public static SequencedMap<Object, Object> sortProperties(final Properties prop, final List<String> order) {
            return prop.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> {
                    final int index = order.indexOf(e.getKey().toString());
                    // If a key isn't in our list, put it at the end
                    return index == -1 ? Integer.MAX_VALUE : index;
                }))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldValue, _) -> oldValue,
                    LinkedHashMap::new // Maintains the sorted insertion order
                ));
        }

        // Private constructor to prevent instantiation
        private ListAndMapSubClass() {
            // intentional empty
        }

    }

    /**
     * Project Properties Reader
     */
    public static final class PropertiesReaderSubClass {

        /**
         * get variable
         * @param strVariables variables to pick
         * @return Properties
         */
        public static Properties getVariableFromProjectProperties(final String propertyFileName, final String... strVariables) {
            final Properties svProperties = new Properties();
            try(InputStream inputStream = PropertiesReaderSubClass.class.getResourceAsStream(propertyFileName)) {
                final Properties inProperties = new Properties();
                inProperties.load(inputStream);
                final List<String> arrayVariables = Arrays.asList(strVariables);
                arrayVariables.forEach(crtVariable -> svProperties.put(crtVariable, inProperties.getProperty(crtVariable)));
                if (!propertyFileName.startsWith("/META-INF/maven/")) {
                    final String strFeedback = String.format("I have successfully loaded entire content from %s file into stream...", svProperties);
                    LogExposureClass.LOGGER.debug(strFeedback);
                }
            } catch (IOException ei) {
                final Path ptPrjProps = Path.of(propertyFileName);
                final String strFeedback = String.format(FileOperationsClass.I18N_FILE_FND_ERR, ptPrjProps.getParent(), ptPrjProps.getFileName());
                LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
            }
            return svProperties;
        }

        private PropertiesReaderSubClass() {
            // intentionally left open
        }

    }

    /**
     * Cleaning things
     */
    public static final class StringCleaningSubClass {

        /**
         * Clean String From CurlyBraces
         * @param strOriginal Original string
         * @return String
         */
        public static String cleanStringFromCurlyBraces(final String strOriginal) {
            final StringBuilder strBuilder = new StringBuilder();
            for (final char c : strOriginal.toCharArray()) {
                if (c != '{' && c != '}') {
                    strBuilder.append(c);
                }
            }
            return strBuilder.toString();
        }

        /**
         * Cleaning string to be used as database object
         * @param strObject input String
         * @return String cleaned
         */
        public static String cleanStringAsDatabaseObject(final String strObject) {
            return strObject.replaceAll("[^A-Za-z0-9_\\/.|()]", "");
        }

        /**
         * Cleaning string from unwanted characters
         * @param strObject input String
         * @return String cleaned
         */
        public static String cleanStringFromUnwantedCharacters(final String strObject) {
            return strObject.replaceAll("[^A-Za-z0-9 _\\-–\\/.():'`]", "");
        }

        /**
         * Ensuring proper escaping for valid JSON
         * @param inString input String
         * @return String with proper escaped characters
         */
        public static String ensureEscapingForValidJson(final String inString) {
            return inString.replace("\n", "\\n").replace("\r", "\\r").replace("\t", " ");
        }

        /**
         * Helper to remove surrounding double quotes safely
         * @param strInput initial String
         * @return String without double quotes enclosing
         */
        public static String stripQuotes(final String strInput) {
            return (strInput != null && strInput.length() >= 2 && strInput.startsWith("\"") && strInput.endsWith("\""))
                    ? strInput.substring(1, strInput.length() - 1)
                    : strInput;
        }

        // Private constructor to prevent instantiation
        private StringCleaningSubClass() {
            // intentionally blank
        }

    }

    /**
     * Conversion things
     */
    public static final class StringConversionSubClass {
        /**
         * Single Question Mark Character
         */
        private static final String Q_MARK_PARAM = "SingleQuestionMarkCharacterParameter";

        /**
         * Convert Prompt Parameters into Named Parameters
         * @param strOriginalQ query with prompt parameter
         * @return query with named parameters
         */
        private static String convertPromptParameters(final String strOriginalQ, final String type) {
            final String strFeedbackStrt = String.format("Original query is: \"%s\"", strOriginalQ);
            LogExposureClass.LOGGER.debug(strFeedbackStrt);
            final List<String> listMatches = RegularExpressionsClass.extractMatches(strOriginalQ, RegularExpressionsClass.STR_PRMTR_RGX);
            String strFinalQ = strOriginalQ;
            if (Q_MARK_PARAM.equalsIgnoreCase(type)) {
                for (final String currentPrmtName : listMatches) {
                    strFinalQ = strFinalQ.replace(currentPrmtName, Character.toString(63));
                }
            } else if (STR_NAMED_PARAM.equalsIgnoreCase(type)) {
                for (final String currentPrmtName : listMatches) {
                    strFinalQ = strFinalQ.replace(currentPrmtName, convertSinglePromptParameterIntoNamedParameter(currentPrmtName));
                }
            }
            final String strFeedbackEnd = String.format("Final query is: \"%s\"", strFinalQ);
            LogExposureClass.LOGGER.debug(strFeedbackEnd);
            return strFinalQ;
        }

        /**
         * Convert Prompt Parameters into Named Parameters
         * @param strOriginalQ query with prompt parameter
         * @return query with named parameters
         */
        public static String convertPromptParametersIntoNamedParameters(final String strOriginalQ) {
            return convertPromptParameters(strOriginalQ, STR_NAMED_PARAM);
        }

        /**
         * Convert Prompt Parameters into Named Parameters
         * @param strOriginalQ query with prompt parameter
         * @return query with named parameters
         */
        public static String convertPromptParametersIntoParameters(final String strOriginalQ) {
            return convertPromptParameters(strOriginalQ, Q_MARK_PARAM);
        }

        /**
         * get Named Parameter From Prompt One
         * @param strOriginal Original string
         * @return String
         */
        private static String convertSinglePromptParameterIntoNamedParameter(final String strOriginal) {
            return ":" + StringCleaningSubClass.cleanStringFromCurlyBraces(strOriginal).replace(" ", "_");
        }

        // Private constructor to prevent instantiation
        private StringConversionSubClass() {
            // intentionally blank
        }

    }

    /**
     * Evaluating things
     */
    public static final class StringEvaluationSubClass {
        /**
         * Maximum Length for Integer
         */
        /* default */ private static final int MAX_LENGTH_INT = 10;
        /**
         * Maximum Integer
         */
        /* default */ private static final int MAX_INT = 2_147_483_647;

        /**
         * Checks if given string is included in a given List of Strings
         * @param str String to search into
         * @param substrings Strings to search for
         * @return boolean true if found, false otherwise
         */
        public static boolean hasMatchingSubstring(final String str, final List<String> substrings) {
            return substrings.stream().anyMatch(str::contains);
        }

        /**
         * Check if String is actually Date
         *
         * @param inputString string to evaluate
         * @return True if given String is actually Date
         */
        public static boolean isStringActuallyDate(final String inputString) {
            return RegularExpressionsClass.ValidationSubClass.isStringActuallySomething(inputString, STR_JUST_DATE);
        }

        /**
         * Check if String is actually Decimal
         *
         * @param inputString string to evaluate
         * @return True if given String is actually Integer
         */
        public static boolean isStringActuallyDecimal(final String inputString) {
            return RegularExpressionsClass.ValidationSubClass.isStringActuallySomething(inputString, "decimal");
        }

        /**
         * Check if String is actually Numeric
         *
         * @param inputString string to evaluate
         * @return True if given String is actually Integer
         */
        public static boolean isStringActuallyInteger(final String inputString) {
            boolean evaluation = RegularExpressionsClass.ValidationSubClass.isStringActuallySomething(inputString, "long");
            if (evaluation
                    && inputString.length() >= MAX_LENGTH_INT) {
                try {
                    final long longValue = Long.parseLong(inputString);
                    if (longValue > MAX_INT) {
                        evaluation = false;
                    }
                } catch (NumberFormatException noFormatException) {
                    final String strFeedback = String.format(CONVERT_INT_NA, inputString, Arrays.toString(noFormatException.getStackTrace()));
                    LogExposureClass.LOGGER.error(strFeedback);
                    evaluation = false;
                }
            }
            return evaluation;
        }

        /**
         * Check if String is actually Long
         *
         * @param inputString string to evaluate
         * @return True if given String is actually Long
         */
        public static boolean isStringActuallyLong(final String inputString) {
            return RegularExpressionsClass.ValidationSubClass.isStringActuallySomething(inputString, "long");
        }

        /**
         * Check if String is actually Long Time-stamp w. milliseconds
         *
         * @param inputString string to evaluate
         * @return True if given String is actually Long Time-stamp w. milliseconds
         */
        public static boolean isStringActuallyLongTimestampWithMilliseconds(final String inputString) {
            return RegularExpressionsClass.ValidationSubClass.isStringActuallySomething(inputString,
                    RegularExpressionsClass.STR_LONG_TS_MSEC);
        }

        /**
         * Check if String is actually Numeric
         *
         * @param inputString string to evaluate
         * @return True if given String is actually Numeric
         */
        public static boolean isStringActuallyNumeric(final String inputString) {
            return RegularExpressionsClass.ValidationSubClass.isStringActuallySomething(inputString, "numeric");
        }

        /**
         * Check if String is actually Time-stamp
         *
         * @param inputString string to evaluate
         * @return True if given String is actually Time-stamp
         */
        public static boolean isStringActuallyTimestamp(final String inputString) {
            return RegularExpressionsClass.ValidationSubClass.isStringActuallySomething(inputString, STR_TIMESTAMP);
        }

        /**
         * Check if String is actually Time-stamp w. milliseconds
         *
         * @param inputString string to evaluate
         * @return True if given String is actually Time-stamp w. milliseconds
         */
        public static boolean isStringActuallyTimestampWithMilliseconds(final String inputString) {
            return RegularExpressionsClass.ValidationSubClass.isStringActuallySomething(inputString, STR_TS_MSEC);
        }

        /**
         * Check if String is actually a Version
         * @param inputString string to evaluate
         * @return True if given String is actually Version
         */
        public static boolean isStringActuallyVersion(final String inputString) {
            return RegularExpressionsClass.ValidationSubClass.isStringActuallySomething(inputString, "version");
        }

        /**
         * check if string follows a pattern
         * @param inString input String
         * @return true/false
         */
        public static boolean isStringOneVariable(final String inString) {
            boolean bolReturn = false;
            if (inString.startsWith("${")
                    && inString.endsWith("}")) {
                bolReturn = true;
            }
            return bolReturn;
        }

        // Private constructor to prevent instantiation
        private StringEvaluationSubClass() {
            // intentionally blank
        }

    }

    /**
     * Transforming things
     */
    public static final class StringTransformationSubClass {

        /**
         * Compute String checksum
         * @param inString input String
         * @return String
         */
        public static String computeStringSignature(final String inString) {
            String outString = "";
            final String algorithm = "SHA-256";
            try {
                // 1. Get the SHA-256 instance
                final MessageDigest digest = MessageDigest.getInstance(algorithm);
                // 2. Perform the hashing
                final byte[] encodedHash = digest.digest(inString.getBytes(StandardCharsets.UTF_8));
                // 3. Convert to Hex String using HexFormat (introduced in Java 17+)
                outString = HexFormat.of().formatHex(encodedHash);
            } catch (NoSuchAlgorithmException e) {
                final String strFeedbackErr = String.format("Checksum algorithm %s is not available.... %s", algorithm, Arrays.toString(e.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
            return outString;
        }

        /**
         * get Named Parameter From Prompt One
         * @param inString Original string
         * @return String
         */
        private static String encloseStringWithCharacter(final String inString, final char inChar) {
            final StringBuilder strBuilder = new StringBuilder();
            if (inString.matches(String.format("^%s.*%s$", inChar, inChar))) { // is already enclosed
                strBuilder.append(inString);
            } else if (inString.matches(String.format("^%s.*[^%s]$", inChar, inChar))) { // has only start enclosed
                strBuilder.append(inString).append('\"');
            } else if (inString.matches(String.format("^[^%s].*%s$", inChar, inChar))) { // has only end enclosed
                strBuilder.append('\"').append(inString);
            } else { // does not have neither start nor end enclosed
                strBuilder.append('\"').append(inString).append('\"');
            }
            return strBuilder.toString();
        }

        /**
         * get Named Parameter From Prompt One
         * @param inString Original string
         * @return String
         */
        public static String encloseStringIfContainsSpace(final String inString, final char inChar) {
            String strReturn = inString;
            if (inString.contains(" ")) {
                strReturn = encloseStringWithCharacter(inString, inChar);
            }
            return strReturn;
        }

        /**
         * Ensures no password exposure
         * @param inProps input Properties
         * @return Properties with certain things obfuscated
         */
        public static Properties obfuscateProperties(final Properties inProps) {
            final Properties outProps = new Properties();
            final String strKeyToObfuscate = "password";
            inProps.forEach((strKey, strValue) -> {
                if (strKey.equals(strKeyToObfuscate)) {
                    outProps.put(strKey, "*U*N*D*I*S*C*L*O*S*E*D*");
                } else {
                    outProps.put(strKey, strValue);
                }
            });
            return outProps;
        }

        // Private constructor to prevent instantiation
        private StringTransformationSubClass() {
            // intentionally blank
        }

    }

    /**
     * Constructor
     */
    private BasicStructuresClass() {
        // intentionally blank
    }
}
