/** Copyright 2026 Daniel-Gheorghe Popiniuc */
package io.github.dgp_eu.tools.databases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.core.ProjectClass;
import io.github.dgp_eu.tools.core.ShellingClass;
import io.github.dgp_eu.tools.databases.DatabaseOperationsClass.ConnectivitySubClass;
import io.github.dgp_eu.tools.databases.DatabaseOperationsClass.ResultSettingSubClass;

/**
 * Snowflake methods
 */
public final class SpecificSnowflakeClass {
    /** String for Snowflake */
    public static final String STR_SNOWFLAKE = "Snowflake";
    /** standard String */
    public static final String STR_ROLES = "Roles";
    /** connection string Variable */
    private static String strConnection;
    /** username variable */
    private static String strUserName;

    /**
     * building Snowflake connection String
     * @param inProperties instance Properties
     * @return Snowflake connection String
     */
    public static String buildConnectionString(final Properties inProperties) {
        String accountId = inProperties.getProperty("AccountIdentifier", "").toString();
        if (accountId.isBlank()) {
            final String strFeedback = "As attribute \"AccountIdentifier\" is missing will attempt building it by combining Organization and AccountLocator";
            LogExposureClass.LOGGER.debug(strFeedback);
            final String strOrganization = inProperties.getProperty("Organization", "").toString().toLowerCase(Locale.getDefault());
            final String accountLocator = inProperties.getProperty("AccountLocator", "").toString().toLowerCase(Locale.getDefault());
            if (strOrganization.isBlank()
                    || accountLocator.isBlank()) {
                final String strFeedbackErr = "Either attribute \"Organization\" or \"AccountLocator\" (or both) is missing creating attribute \"AccountIdentifier\" required for Snowflake connection string is not possible...";
                LogExposureClass.LOGGER.error(strFeedbackErr);
                throw new RuntimeException(strFeedbackErr);
            }
            accountId = String.format("%s-%s.privatelink", strOrganization, accountLocator);
        }
        final String outConnection = String.format("jdbc:snowflake://%s.snowflakecomputing.com/", accountId);
        final String strFeedback = String.format("Snowflake connection String has been build: %s", outConnection);
        LogExposureClass.LOGGER.debug(strFeedback);
        return outConnection;
    }

    /**
     * Initiate a Snowflake connection with Instance properties and DB specified
     *
     * @param propInstance instance properties
     * @param strDatabase database to connect to
     * @return Connection
     */
    public static Connection getSnowflakeConnection(final Properties propInstance, final String strDatabase) {
        loadSnowflakeDriver();
        final Properties propConnection = getSnowflakeProperties(strDatabase, propInstance);
        Connection connection = null;
        try {
            final String strFeedback = String.format("Will attempt to create a %s connection to database %s using %s as connection string and %s properties",
                    STR_SNOWFLAKE,
                    strDatabase,
                    strConnection,
                    propConnection);
            LogExposureClass.LOGGER.debug(strFeedback);
            connection = DriverManager.getConnection(strConnection, propConnection);
            final String strFeedbackOk = String.format("%s connection to database %s was successfully established!",
                    STR_SNOWFLAKE, 
                    strDatabase);
            LogExposureClass.LOGGER.debug(strFeedbackOk);
        } catch (SQLException e) {
            final String strFeedbackErr = String.format("%s connection has failed %s",
                    STR_SNOWFLAKE,
                    e.getLocalizedMessage());
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
        return connection;
    }

    /**
     * Retrieving Snowflake JDBC driver version
     * @return String
     */
    private static String getSnowflakeJdbcDriverVersion() {
        final String vSnowflakeId = "snowflake.jdbc";
        String vJdbcVersion = null;
        String vFoundIn = null;
        final Map<String, Object> moduleMap = ProjectClass.getProjectModuleLibraries();
        if (moduleMap.containsKey(vSnowflakeId)) {
            vJdbcVersion = moduleMap.get(vSnowflakeId).toString();
            vFoundIn = "Modules";
        } else {
            ProjectClass.loadProjectModel();
            ProjectClass.LoaderSubClass.loadComponents();
            final Map<String, Object> projDependencies = ProjectClass.ComponentsSubClass.getProjectModelComponent("Dependencies");
            if (projDependencies.containsKey(vSnowflakeId)) {
                vJdbcVersion = projDependencies.get(vSnowflakeId).toString();
                vFoundIn = "Dependencies";
            }
        }
        final String strFeedback = String.format("I have found Snowflake JDBC driver v.%s from %s", vJdbcVersion, vFoundIn);
        LogExposureClass.LOGGER.debug(strFeedback);
        return vJdbcVersion;
    }

    /**
     * get standardized Information from Snowflake
     *
     * @param objStatement statement
     * @param strAction which action
     * @param strFetchType kind of output
     * @return List of Properties
     */
    public static List<Properties> getSnowflakePreDefinedInformation(final Statement objStatement, final String strAction, final String strFetchType) {
        final Properties queryProperties = new Properties();
        if (STR_ROLES.equalsIgnoreCase(strAction)) {
            queryProperties.put("expectedExactNumberOfColumns", "1");
        }
        final String strQueryToUse = DatabaseOperationsClass.getPreDefinedQuery(STR_SNOWFLAKE, strAction);
        final Properties rsProperties = DatabaseOperationsClass.packageResultSetProperties("purpose: " + strAction, strQueryToUse, strFetchType);
        return ResultSettingSubClass.getResultSetStandardized(objStatement, rsProperties, queryProperties);
    }

    /**
     * build Snowflake Properties
     *
     * @param strDatabase Database name to connect to
     * @param propInstance instance properties
     * @return Properties
     */
    private static Properties getSnowflakeProperties(final String strDatabase, final Properties propInstance) {
        final Properties properties = new Properties();
        properties.put("user", getUsernameForConnection());
        properties.put("db", strDatabase);
        properties.put("allowUnderscoresInHost", true);
        properties.put("JDBC_QUERY_RESULT_FORMAT", "JSON"); // overwrite default values which is Arrow
        final List<String> optionalProperties = List.of("Authenticator", "Role", "Schema", "Warehouse");
        optionalProperties.forEach(strValue -> {
            final String strCrtValue = propInstance.getOrDefault(strValue, "").toString();
            if (!strCrtValue.isBlank()) {
                properties.put(strValue.toLowerCase(Locale.getDefault()), strCrtValue.replace("\"", ""));
            }
        });
        properties.put("tracing", "SEVERE"); // to hide INFO and Warnings which are visible otherwise
        final String strFeedback = String.format("Snowflake connection Properties are: %s", properties);
        LogExposureClass.LOGGER.debug(strFeedback);
        return properties;
    }

    /**
     * Logic to retrieve relevant user-name for Snowflake connection
     * @return String with user-name
     */
    private static String getUsernameForConnection() {
        String currentUser = strUserName;
        if (currentUser == null) {
            final String resolvedUser = ShellingClass.getCurrentUserAccount();
            currentUser = resolvedUser == null ? null : resolvedUser.toUpperCase(Locale.getDefault());
        }
        if (currentUser == null
                || currentUser.isEmpty()) {
            currentUser = "UNKNOWN_USER";
        }
        return currentUser;
    }

    /**
     * Loading Snowflake driver
     */
    private static void loadSnowflakeDriver() {
        final String jdbcVersion = getSnowflakeJdbcDriverVersion();
        final String strDriverName = "net.snowflake.client.jdbc.SnowflakeDriver";
        final String strFeedback = String.format("Will attempt to load %s driver %s", STR_SNOWFLAKE, strDriverName);
        LogExposureClass.LOGGER.debug(strFeedback);
        try {
            Class.forName(strDriverName);
            final String strFeedbackOk = String.format("%s driver %s has been successfully loaded", STR_SNOWFLAKE, strDriverName + " v. " + jdbcVersion);
            LogExposureClass.LOGGER.debug(strFeedbackOk);
        } catch (ClassNotFoundException ex) {
            final String strFeedbackErr = String.format("%s driver %s not found... %s", STR_SNOWFLAKE, strDriverName, Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
    }

    /**
     * Execute Snowflake pre-defined actions
     * @param strAction which action to perform
     * @param objProps object properties
     */
    public static void performSnowflakePreDefinedAction(final String strAction, final Properties objProps) {
        try (Connection objConnection = getSnowflakeConnection(objProps, objProps.get("databaseName").toString())) {
            if (objConnection == null) {
                throw new SQLException("Snowflake connection is null.");
            }
            try (Statement objStatement = ConnectivitySubClass.createSqlStatement(STR_SNOWFLAKE, objConnection)) {
                final List<Properties> predefinedInfo = getSnowflakePreDefinedInformation(objStatement, strAction, DatabaseOperationsClass.STR_VALUES);
                LogExposureClass.LOGGER.info(predefinedInfo.toString());
            }
        } catch (SQLException e) {
            final String strFeedback = String.format("Error \"%s\"", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
    }

    /**
     * Setter for strConnection
     * @param inConnString imposed Snowflake connection String
     */
    public static void setConnectionString(final String inConnString) {
        strConnection = inConnString;
    }

    /**
     * Setter for strUserName
     * @param inUsername imposed username for Snowflake connectivity
     */
    public static void setUsernameForConnection(final String inUsername) {
        strUserName = inUsername;
    }

    /**
     * Constructor
     */
    private SpecificSnowflakeClass() {
        // intentionally blank
    }
}