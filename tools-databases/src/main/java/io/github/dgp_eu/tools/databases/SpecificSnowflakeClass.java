/**
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
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
    /** standard String */
    private static String strUserName;

    /**
     * Snowflake Bootstrap
     *
     * @param objStatement statement
     */
    public static void executeSnowflakeBootstrapQuery(final Statement objStatement) {
        final String strQueryToUse = "ALTER SESSION SET JDBC_QUERY_RESULT_FORMAT='JSON';";
        DatabaseOperationsClass.executeQueryWithoutResultSet(objStatement, "Bootstrap", strQueryToUse);
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
        Connection connection = null;
        final String strConnection = String.format("jdbc:snowflake://%s.snowflakecomputing.com/", propInstance.get("AccountName").toString().replace("\"", ""));
        final Properties propConnection = getSnowflakeProperties(strDatabase, propInstance);
        final String strFeedback = String.format("Will attempt to create a %s connection to database %s using %s as connection string and %s properties", STR_SNOWFLAKE, strDatabase, strConnection, propConnection);
        LogExposureClass.LOGGER.debug(strFeedback);
        try {
            connection = DriverManager.getConnection(strConnection, propConnection);
            final String strFeedbackOk = String.format("%s connection to database %s was successfully established!", STR_SNOWFLAKE, strDatabase);
            LogExposureClass.LOGGER.debug(strFeedbackOk);
        } catch (SQLException e) {
            final String strFeedbackErr = String.format("%s connection has failed %s", STR_SNOWFLAKE, e.getLocalizedMessage());
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
        final List<String> optionalProperties = List.of("Authenticator", "Role", "Schema", "Warehouse");
        optionalProperties.forEach(strValue -> {
            final String strCrtValue = propInstance.getOrDefault(strValue, "").toString();
            if (!strCrtValue.isBlank()) {
                properties.put(strValue.toLowerCase(Locale.US), strCrtValue.replace("\"", ""));
            }
        });
        properties.put("tracing", "SEVERE"); // to hide INFO and Warnings which are visible otherwise
        return properties;
    }

    /**
     * Logic to retrieve relevant user-name for Snowflake connection
     * @return String with user-name
     */
    private static String getUsernameForConnection() {
        String currentUser = strUserName;
        if (strUserName == null) {
            currentUser = ShellingClass.getCurrentUserAccount().toUpperCase(Locale.getDefault());
        }
        if (currentUser.isEmpty()) {
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
            assert objConnection != null;
            try (Statement objStatement = ConnectivitySubClass.createSqlStatement(STR_SNOWFLAKE, objConnection)) {
                executeSnowflakeBootstrapQuery(objStatement);
                final List<Properties> predefinedInfo = getSnowflakePreDefinedInformation(objStatement, strAction, DatabaseOperationsClass.STR_VALUES);
                LogExposureClass.LOGGER.info(predefinedInfo.toString());
            }
        } catch (SQLException e) {
            final String strFeedback = String.format("Error \"%s\"", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
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