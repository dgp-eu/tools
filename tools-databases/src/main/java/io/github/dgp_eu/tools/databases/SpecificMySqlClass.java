/** Copyright 2026 Daniel-Gheorghe Popiniuc */
package io.github.dgp_eu.tools.databases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.databases.DatabaseOperationsClass.ConnectivitySubClass;
import io.github.dgp_eu.tools.databases.DatabaseOperationsClass.ResultSettingSubClass;


/**
 * MySQL methods
 */
public final class SpecificMySqlClass {
    /**
     * Database MySQL
     */
    public static final String STR_DB_MYSQL = "MySQL";

    /**
     * Initiate a MySQL connection with Instance properties and DB specified
     *
     * @param propInstance Properties for Instance
     * @param strDatabase Database to connect to
     * @return Connection
     */
    public static Connection getMySqlConnection(final Properties propInstance, final String strDatabase) {
        Connection connection = null;
        if (propInstance.isEmpty()) {
            final String strFeedbackErr = String.format("%s connection properties seems to be empty, hence connection cannot be initiated...", STR_DB_MYSQL);
            LogExposureClass.LOGGER.error(strFeedbackErr);
        } else {
            final String strServer = propInstance.get("ServerName").toString();
            final int strPort = BasicStructuresClass.convertStringIntoInteger(propInstance.get("Port").toString());
            try {
                final String strConnection = String.format("jdbc:mysql://%s:%s/%s", strServer, strPort, strDatabase);
                final Properties propConnection = getMySqlProperties(propInstance);
                final String strFeedback = String.format("Will attempt to create a %s connection to database %s using %s as connection string and %s properties", STR_DB_MYSQL, strDatabase, strConnection, BasicStructuresClass.StringTransformationSubClass.obfuscateProperties(propConnection));
                LogExposureClass.LOGGER.debug(strFeedback);
                connection = DriverManager.getConnection(strConnection, propConnection);
                final String strFeedbackOk = String.format("%s connection to server %s, port %s and database %s was successfully established!", STR_DB_MYSQL, strServer, strPort, strDatabase);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
            } catch(SQLException e) {
                final String strFeedbackErr = String.format("%s connection to server %s, port %s and database %s has failed %s", STR_DB_MYSQL, strServer, strPort, strDatabase, e.getLocalizedMessage());
                LogExposureClass.LOGGER.debug(strFeedbackErr);
            }
        }
        return connection;
    }

    /**
     * get standardized Information from MySQL
     *
     * @param objStatement Statement
     * @param strWhich Which query is needed
     * @param strFetchType which type of output would be needed
     * @return List with Properties
     */
    public static List<Properties> getMySqlPreDefinedInformation(final Statement objStatement, final String strWhich, final String strPurpose, final String strFetchType) {
        final String strQueryToUse = DatabaseOperationsClass.getPreDefinedQuery(STR_DB_MYSQL, strWhich);
        final Properties rsProperties = DatabaseOperationsClass.packageResultSetProperties(strPurpose, strQueryToUse, strFetchType);
        return ResultSettingSubClass.getResultSetStandardized(objStatement, rsProperties, new Properties());
    }

    /**
     * get MySQL Properties
     *
     * @param propInstance Instance Properties
     * @return Properties
     */
    private static Properties getMySqlProperties(final Properties propInstance) {
        final Properties properties = new Properties();
        properties.put("user", propInstance.get("Username").toString());
        properties.put("password", propInstance.get("Password").toString());
        properties.put("serverTimezone", propInstance.get("ServerTimezone").toString());
        properties.put("autoReconnect", true);
        properties.put("allowPublicKeyRetrieval", true);
        properties.put("useSSL", false);
        properties.put("useUnicode", true);
        properties.put("useJDBCCompliantTimezoneShift", true);
        properties.put("useLegacyDatetimeCode", false);
        properties.put("characterEncoding", "UTF-8");
        return properties;
    }

    /**
     * Execute MySQL pre-defined actions
     *
     * @param strWhich Which kind of query is needed
     * @param givenProperties Connection Properties
     */
    public static void performMySqlPreDefinedAction(final String strWhich, final Properties givenProperties) {
        try (Connection objConnection = getMySqlConnection(givenProperties, "mysql")) {
            assert objConnection != null;
            try (Statement objStatement = ConnectivitySubClass.createSqlStatement(STR_DB_MYSQL, objConnection)) {
                final List<Properties> listProps = getMySqlPreDefinedInformation(objStatement, strWhich, "purpose " + strWhich, DatabaseOperationsClass.STR_VALUES);
                if (!listProps.isEmpty()) {
                    final String strFeedback = listProps.toString();
                    LogExposureClass.LOGGER.info(strFeedback);
                }
            }
        } catch(SQLException e) {
            final String strFeedbackErr = String.format("Error %s", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
    }

    /**
     * constructor
     */
    private SpecificMySqlClass() {
        // intentionally blank
    }
}