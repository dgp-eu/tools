package io.github.dgp_eu.tools.core;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Configuration strings management
 */
public final class ConfigurationClass {
    /** Constant for non or single/one */
    /* default */ public static final String ARITY_NONE_OR_ONE = "0..1";
    /** arity one or more */
    /* default */ public static final String ARITY_ONE_OR_MORE = "1..*";
    /** One as string */
    /* default */ public static final String ARITY_ONLY_ONE = "1";
    /** frequently expression used to catch conversion error  */
    public static final String CONVERT_INT_NA = "Could not convert value %s into Integer... %s";
    /** default Locale */
    public static final String DEFAULT_LOCALE = "en-US";
    /** "Active Pixels" constant */
    public static final String STR_ACTV_PXLS = "Active Pixels";
    /** binary constant */
    public static final String STR_BINARY = "binary";
    /** Content constant */
    public static final String STR_CONTENT = "Content";
    /** decimal constant */
    public static final String STR_DECIMAL = "decimal";
    /** Dependencies constant */
    public static final String STR_DEPENDENCIES = "Dependencies";
    /** String for internal ETL */
    public static final String STR_DOT_THREE = "DotAndThreeDigitNumber";
    /** String for internal Environment */
    public static final String STR_ENV = "Environment";
    /** String for internal Environment Details */
    public static final String STR_ENV_DTLS = "EnvironmentDetails";
    /** Constant for error */
    public static final String STR_ERROR = "error";
    /** String for internal Executing query successful */
    public static final String STR_EXEC_QRY_OK = "Executing %s query was successful!";
    /** Constant for File Hashing */
    public static final String STR_FILE_HASHING = "FileHashing";
    /** Firmware string */
    public static final String STR_FIRMWARE = "Firmware";
    /** HumanReadableTime constant */
    public static final String STR_TM_HUMAN = "HumanReadableTime";
    /** HumanReadableTimeWithMilliseconds constant */
    public static final String STR_TM_HUMAN_MS = "HumanReadableTimeWithMilliseconds";
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
    /** Millisecond string constant */
    public static final String STR_MILLISECOND = "Millisecond";
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
    /** Variable for Current Folder */
    private static String currentFolder;

    static {
        loadCurrentFolder();
    }

    /**
     * Getting current project folder
     * @return application folder
     */
    private static void loadCurrentFolder() {
        String strAppFolder = "";
        final File directory = new File(""); // parameter is empty
        try {
            strAppFolder = directory.getCanonicalPath();
        } catch (IOException ex) {
            final String strFeedback = String.format("Error encountered in getting folder... %s",
                    Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        currentFolder = strAppFolder;
    }

    /**
     * Getter for currentFolder
     * @return String
     */
    public static String getCurrentFolder() {
        return currentFolder;
    }

    /**
     * reads Environment Variable into String
     * @param inEnvVariable input Environment Variable name
     * @return String with value found
     */
    public static String getEnvironmentVariableValue(final String inEnvVariable) {
        final String strEnvValue = System.getenv(inEnvVariable);
        if (strEnvValue == null) {
            final String strFeedback = String.format("Environment variable %s not found!", inEnvVariable);
            LogExposureClass.LOGGER.error(strFeedback);
            throw new IllegalArgumentException(strFeedback);
        }
        final String strFeedback = String.format("Environment variable %s was found successfully!", inEnvVariable);
        LogExposureClass.LOGGER.debug(strFeedback);
        return strEnvValue;
    }

    // Private constructor to prevent instantiation
    private ConfigurationClass() {
        super();
    }

}