/*
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.databases;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

import org.sqlite.Function;

import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.FileOperationsClass;
import io.github.dgp_eu.tools.core.HtmlClass;
import io.github.dgp_eu.tools.core.JsonOperationsClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.core.ProjectClass;
import io.github.dgp_eu.tools.core.RegularExpressionsClass;
import io.github.dgp_eu.tools.core.ShellingClass;
import io.github.dgp_eu.tools.core.TimingClass;
import tools.jackson.databind.JsonNode;

/**
 * Database methods
 */
public final class DatabaseOperationsClass {
    /** NULL string */
    public static final String STR_NULL = "NULL";
    /** Regular Expression for Prompt Parameters within SQL Query */
    public static final String STR_QTD_STR_VL = "\"%s\"";
    /** Values string */
    public static final String STR_VALUES = "Values";

    /**
     * Fill values into a dynamic query
     * @param queryProperties properties for connection
     * @param strRawQuery raw query
     * @param arrayCleanable array with fields to clean
     * @param arrayNullable array with NULL-able fields
     * @return final query
     */
    public static String distributePropertiesToQuery(final Properties queryProperties, final String strRawQuery, final String[] arrayCleanable, final String... arrayNullable) {
        String strQueryToReturn = strRawQuery;
        for (final Object obj : queryProperties.keySet()) {
            final String strKey = (String) obj;
            final String strOriginalValue = queryProperties.getProperty(strKey);
            String strValueToUse = String.format(STR_QTD_STR_VL, strOriginalValue);
            if (strOriginalValue.matches(STR_NULL)) {
                strValueToUse = strOriginalValue;
            } else if (Arrays.asList(arrayCleanable).contains(strKey)) {
                strValueToUse = String.format(STR_QTD_STR_VL, strOriginalValue.replaceAll("([\"'])", ""));
                if (strOriginalValue.isEmpty()) {
                    strValueToUse = STR_NULL;
                }
            } else if (Arrays.asList(arrayNullable).contains(strKey) && strOriginalValue.isEmpty()) {
                strValueToUse = STR_NULL;
            } else if (strKey.contains("_JSON") || strKey.startsWith("JSON_")) {
                strValueToUse = String.format(STR_QTD_STR_VL, strOriginalValue.replace("\"", "\"\""));
            }
            strQueryToReturn = strQueryToReturn.replace(String.format("{%s}", strKey), strValueToUse);
        }
        return strQueryToReturn;
    }

    /**
     * Execute a custom query with result-set expected
     * @param objStatement statement
     * @param strPurpose query purpose
     * @param strQueryToUse query to use
     * @param objProperties properties (with features to apply)
     * @return ResultSet
     */
    public static ResultSet executeCustomQuery(final Statement objStatement, final String strPurpose, final String strQueryToUse, final Properties objProperties) {
        ResultSet resultSet = null;
        if (strQueryToUse != null) {
            final LocalDateTime startTimeStamp = LocalDateTime.now(ZoneId.systemDefault());
            final String strFeedbackAtmpt = String.format("Will attempt to execute %s query which is defined as: %s", strPurpose, strQueryToUse);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            try {
                resultSet = objStatement.executeQuery(strQueryToUse);
                final String strFeedbackOk = String.format(BasicStructuresClass.STR_EXEC_QRY_OK, strPurpose);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
                ResultSettingSubClass.digestCustomQueryProperties(strPurpose, resultSet, objProperties);
            } catch (SQLException e) {
                final String strFeedback = String.format("Statement execution for %s has failed with following error: %s", strPurpose, e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedback);
            }
            final LocalDateTime finishTimeStamp = LocalDateTime.now(ZoneId.systemDefault());
            final String strFeedbackEnd = TimingClass.logDuration(startTimeStamp, finishTimeStamp, String.format("Finished executing SQL query %s", strPurpose));
            LogExposureClass.LOGGER.debug(strFeedbackEnd);
        }
        return resultSet;
    }

    /**
     * Execute a custom query w/o any result-set
     *
     * @param objStatement statement
     * @param strPurpose purpose of query
     * @param strQueryToUse query to use
     */
    public static void executeQueryWithoutResultSet(final Statement objStatement, final String strPurpose, final String strQueryToUse) {
        if (strQueryToUse != null) {
            final LocalDateTime startTimeStamp = LocalDateTime.now(ZoneId.systemDefault());
            final String strFeedbackAtmpt = String.format("Will attempt to execute %s query", strPurpose);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            try {
                if (strQueryToUse.startsWith("INSERT INTO")) {
                    objStatement.executeLargeUpdate(strQueryToUse);
                } else {
                    objStatement.execute(strQueryToUse);
                }
                final String strFeedback = String.format(BasicStructuresClass.STR_EXEC_QRY_OK, strPurpose);
                LogExposureClass.LOGGER.info(strFeedback);
            } catch (SQLException e) {
                final String strFeedbackErr = String.format("SQL query execution for %s purpose has failed: %s... %s", strPurpose, e.getLocalizedMessage(), Arrays.toString(e.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
            final LocalDateTime finishTimeStamp = LocalDateTime.now(ZoneId.systemDefault());
            final String strFeedbackEnd = TimingClass.logDuration(startTimeStamp, finishTimeStamp, String.format("SQL query execution finished for %s purpose", strPurpose));
            LogExposureClass.LOGGER.debug(strFeedbackEnd);
        }
    }

    /**
     * get order of Prompt Parameters within Query
     * @param strOriginalQ query to consider expected to have Prompt Parameters
     * @param objValues list with Values as List of Properties
     * @return List of Strings with order as value
     */
    public static List<String> getPromptParametersOrderWithinQuery(final String strOriginalQ, final List<Properties> objValues) {
        final List<String> valFields = new ArrayList<>();
        objValues.getFirst().forEach((strKey, _) -> valFields.add(strKey.toString()));
        final String strFeedbackPrmV = String.format("Parameters values are %s", valFields);
        LogExposureClass.LOGGER.debug(strFeedbackPrmV);
        final List<String> listMatches = RegularExpressionsClass.extractMatches(strOriginalQ, RegularExpressionsClass.STR_PRMTR_RGX);
        final String strFeedbackPrm = String.format("Parameters for query are %s", listMatches);
        LogExposureClass.LOGGER.debug(strFeedbackPrm);
        final List<String> mapParameterOrder = new ArrayList<>();
        final int intParameters = listMatches.size();
        for (final String listMatch : listMatches) {
            final String crtParameter = BasicStructuresClass.StringCleaningSubClass.cleanStringFromCurlyBraces(listMatch);
            final int intPosition = valFields.indexOf(crtParameter);
            if (intPosition != -1) {
                mapParameterOrder.add(crtParameter);
            }
        }
        final String strFeedbackPrmM = String.format("Mapping Parameters are %s", mapParameterOrder);
        LogExposureClass.LOGGER.debug(strFeedbackPrmM);
        final int foundParameters = mapParameterOrder.size();
        if (foundParameters != intParameters) {
            final String strFeedback = String.format("Seems we have a problem as %d parameters are expected but only %d were given %s for query \"%s\""
                , intParameters
                , foundParameters
                , mapParameterOrder + " vs. " + objValues.getFirst().toString()
                , strOriginalQ);
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return mapParameterOrder;
    }

    /**
     * Returns standard query
     * @param strFileName query file name needed
     * @return Query as String
     */
    public static String getPreDefinedQuery(final String strDatabaseType, final String strFileName) {
        String strFilePath = String.format("/SQL/%s/%s.sql", strDatabaseType, strFileName);
        final long fileSizeActual = FileOperationsClass.RetrievingSubClass.getInternalFileSize(strFilePath);
        final String strFeedback = String.format("Relevant query file is %s which has a size of %s bytes", strFilePath, fileSizeActual);
        LogExposureClass.LOGGER.debug(strFeedback);
        final long fileSizeLimit = 10;
        if (fileSizeActual < fileSizeLimit) {
            final String strFeedbackErr = LogExposureClass.getUnsupportedFeatures(strFileName, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
            throw new UnsupportedOperationException(strFeedbackErr);
        }
        if (!BasicStructuresClass.isRunningFromJar()) {
            strFilePath = BasicStructuresClass.getCurrentFolder() + "/src/main/resources" + strFilePath;
        }
        return FileOperationsClass.ContentReadingSubClass.getFileContentIntoString(strFilePath);
    }

    /**
     * Package 3 String into Properties for result-set
     * @param strPurpose Which query is needed
     * @param strQueryToUse relevant query
     * @param strFetchType type of output expected
     * @return Properties for result-set
     */
    public static Properties packageResultSetProperties(final String strPurpose, final String strQueryToUse, final String strFetchType) {
        final Properties rsProperties = new Properties();
        rsProperties.put("Purpose", strPurpose);
        rsProperties.put("QueryToUse", strQueryToUse);
        rsProperties.put("FetchType", strFetchType);
        return rsProperties;
    }

    /**
     * Database connectivity
     */
    public static final class ConnectivitySubClass {

        /**
         * Connection closing
         *
         * @param strDatabaseType type of database (mainly for meaningful feedback)
         * @param givenConnection connection object
         */
        public static void closeConnection(final String strDatabaseType, final Connection givenConnection) {
            final String strFeedbackAtmpt = String.format("Will attempt to close a %s SQL connection", strDatabaseType);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            try {
                givenConnection.close();
                final String strFeedbackOk = String.format("A %s SQL connection was successfully closed!", strDatabaseType);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
            } catch (SQLException e) {
                final String strFeedbackErr = String.format("SQL connection of type %s closing failed: %s", strDatabaseType, e.getLocalizedMessage());
                LogExposureClass.LOGGER.debug(strFeedbackErr);
            }
        }

        /**
         * Statement closing
         *
         * @param strDatabaseType type of database (mainly for meaningful feedback)
         * @param givenStatement statement
         */
        public static void closeStatement(final String strDatabaseType, final Statement givenStatement) {
            final String strFeedbackAtmpt = String.format("Will attempt to close a %s SQL statement", strDatabaseType);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            try {
                givenStatement.close();
                final String strFeedbackOk = String.format("A %s SQL statement was successfully closed!", strDatabaseType);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
            } catch (SQLException e) {
                final String strFeedbackErr = String.format("SQL statement of type %s closing failed: %s", strDatabaseType, e.getLocalizedMessage());
                LogExposureClass.LOGGER.debug(strFeedbackErr);
            }
        }

        /**
         * Instantiating a statement
         *
         * @param strDatabaseType type of database (mainly for meaningful feedback)
         * @param connection connection to use
         * @return Statement
         */
        public static Statement createSqlStatement(final String strDatabaseType, final Connection connection) {
            final String strFeedbackAtmpt = String.format("Will attempt to instantiate a %s SQL statement", strDatabaseType);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            Statement objStatement = null;
            try {
                objStatement = connection.createStatement();
                final String strFeedbackOk = String.format("A %s SQL statement was successfully instantiated!", strDatabaseType);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
            } catch (SQLException e) {
                final String strFeedbackErr = String.format("SQL statement creation failed: %s", e.getLocalizedMessage());
                LogExposureClass.LOGGER.debug(strFeedbackErr);
            }
            return objStatement;
        }

        /**
         * Constructor
         */
        private ConnectivitySubClass() {
            // intentionally blank
        }

    }

    /**
     * Database Query Binding
     */
    public static final class QueryBindingSubClass {
        /**
         * variable for Batch Size
         */
        /* default */ private static int batchSize = 1000;

        /**
         * Values to be added for bulk operations
         * @param objConnection Connection for destination Database
         * @param strQueryPurpose Purpose for query execution
         * @param objValues Values to use for executions
         * @param strQuery Original Query with Prompt Parameters
         * @param specialFields Clean-able and Null-able fields
         */
        public static void executeValuesIntoDatabaseUsingPreparedStatement(final Connection objConnection, final String strQueryPurpose, final List<Properties> objValues, final String strQuery, final Properties specialFields) {
            final int intRows = objValues.size();
            if (intRows == 0) {
                final String strFeedback = String.format("Within %s a request to process %s rows was given...", StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName())), intRows);
                LogExposureClass.LOGGER.warn(strFeedback);
            } else {
                final List<String> mapParameterOrder = getPromptParametersOrderWithinQuery(strQuery, objValues);
                final int intParameters = mapParameterOrder.size();
                final String strFinalQuery = BasicStructuresClass.StringConversionSubClass.convertPromptParametersIntoParameters(strQuery);
                try (PreparedStatement preparedStatement = objConnection.prepareStatement(strFinalQuery)) {
                    final Properties properties = new Properties();
                    // cycle through each row
                    for (int crtRow = 1; crtRow <= intRows; crtRow++) {
                        final Properties currentProps = objValues.get(crtRow - 1);
                        // cycle through every single Parameter to set its value to PreparedStatement
                        for (int intParameter = 0; intParameter < intParameters; intParameter++) {
                            final int index = intParameter + 1;
                            final String strKey = mapParameterOrder.get(intParameter);
                            final String strOriginalValue = currentProps.getProperty(strKey);
                            properties.put(BasicStructuresClass.STR_INDEX, index);
                            properties.put("strKey", strKey);
                            properties.put("strOriginalValue", strOriginalValue);
                            properties.put("strQuery", strQuery);
                            properties.put("strArrayCleanable", specialFields.get("Cleanable").toString());
                            properties.put("strArrayNullable", specialFields.get("Nullable").toString());
                            bindSingleParameter(preparedStatement, properties);
                        }
                        preparedStatement.addBatch();
                        if ((crtRow % batchSize == 0)
                                || (crtRow == intRows)) { // each batchSize rows OR final one
                            preparedStatement.executeLargeBatch();
                            final String strFeedback = String.format(BasicStructuresClass.STR_EXEC_QRY_OK, strQueryPurpose + " record " + crtRow);
                            LogExposureClass.LOGGER.info(strFeedback);
                        }
                    }
                } catch (SQLException e) {
                    setSqlExceptionError(e, objValues, strQuery);
                    throw (IllegalStateException)new IllegalStateException().initCause(e);
                }
            }
        }

        /**
         * bind Single Parameter
         * @param preparedStatement original Prepared Statement
         * @param properties properties with relevant components
         */
        private static void bindSingleParameter(final PreparedStatement preparedStatement, final Properties properties) {
            final String strIndex = properties.get(BasicStructuresClass.STR_INDEX).toString();
            final int index = BasicStructuresClass.convertStringIntoInteger(strIndex);
            final String strKey = properties.get("strKey").toString();
            final String strQuery = properties.get("strQuery").toString();
            final String strOriginalValue = properties.get("strOriginalValue").toString();
            final String[] arrayCleanable = properties.get("strArrayCleanable").toString().split("\\|");
            final String[] arrayNullable = properties.get("strArrayNullable").toString().split("\\|");
            try {
                if (STR_NULL.equalsIgnoreCase(strOriginalValue)
                        || (Arrays.asList(arrayNullable).contains(strKey)
                        && strOriginalValue.isEmpty())) {
                    preparedStatement.setNull(index, Types.VARCHAR);
                } else if (Arrays.asList(arrayCleanable).contains(strKey)) {
                    final String strCleanedValue = strOriginalValue.replaceAll("([\"'])", "");
                    if (strCleanedValue.isEmpty()) {
                        preparedStatement.setNull(index, Types.VARCHAR);
                    } else {
                        preparedStatement.setString(index, strCleanedValue);
                    }
                } else if (strKey.contains("_JSON") || strKey.startsWith("JSON_")) {
                    preparedStatement.setString(index, strOriginalValue.replace("\"", "\"\""));
                } else {
                    preparedStatement.setString(index, strOriginalValue);
                }
            } catch (SQLException e) {
                setSqlParameterBindingError(e, strKey, strQuery);
            }
        }

        /**
         * Setter for Batch Size
         * @param inBatchSize the batch size for database operations
         */
        public static void setBatchSize(final int inBatchSize) {
            batchSize = inBatchSize;
        }

        /**
         * Error logging the SQL Exception
         * @param exptObj exception object
         * @param objValues values provided
         * @param strQuery relevant query
         */
        private static void setSqlExceptionError(final SQLException exptObj, final List<Properties> objValues, final String strQuery) {
            final String strFeedback = String.format("%s with Values %s for Query %s", exptObj.getLocalizedMessage(), objValues.getFirst().toString(), strQuery);
            LogExposureClass.LOGGER.error(strFeedback);
        }

        /**
         * Success confirmation to Info log
         * @param exptObj SQLException
         * @param strParameterName parameter name
         * @param strQuery query
         */
        private static void setSqlParameterBindingError(final SQLException exptObj, final String strParameterName, final String strQuery) {
            final String strFeedback = String.format("Error %s when attempting to bind parameter %s to Query %s..."
                    , exptObj.getLocalizedMessage()
                    , strParameterName
                    , strQuery);
            LogExposureClass.LOGGER.error(strFeedback);
        }

        /**
         * Constructor
         */
        private QueryBindingSubClass() {
            // intentionally blank
        }

    }

    /**
     * Basic features for Databases
     */
    public static final class ResultSettingSubClass {
        /**
         * standard SQL statement unable
         */
        public static final String STR_I18N_STM_UNB = "Unable to get the number of %s in the ResultSet... %s";
        /**
         * rows for result set
         */
        private static int intResultSetRows;
        /**
         * column counter
         */
        private static int intColumnsIs;

        /**
         * capture to Log result-set properties
         * @param key current key within loop
         * @param strPurpose query purpose for log text
         * @param objProperties object properties
         */
        private static void captureToLogResultsetAttributes(final String key, final String strPurpose, final Properties objProperties) {
            switch (key) {
                case "expectedExactNumberOfColumns":
                    final int intColumnsShould = BasicStructuresClass.convertStringIntoInteger(objProperties.getProperty(key));
                    final String strFeedbackC = String.format("For the \"%s\" query the Result Set was expected to have exact %s column(s) but a %s was/were found...", strPurpose, intColumnsShould, intColumnsIs);
                    LogExposureClass.LOGGER.error(strFeedbackC);
                    break;
                case "expectedExactNumberOfRows":
                    final int intRowsShould = BasicStructuresClass.convertStringIntoInteger(objProperties.getProperty(key));
                    if (intResultSetRows != intRowsShould) {
                        final String strFeedbackExR = String.format("For the \"%s\" query the Result Set was expected to have exact %s row(s) but a %s was/were found...", strPurpose, intRowsShould, intResultSetRows);
                        LogExposureClass.LOGGER.error(strFeedbackExR);
                    }
                    break;
                case "exposeNumberOfColumns":
                    final String strFeedback = String.format("Number of columns retrieved is %d", intColumnsIs);
                    LogExposureClass.LOGGER.info(strFeedback);
                    break;
                case "exposeNumberOfRows":
                    final String strFeedbackN = String.format("Number of rows retrieved is %d", intResultSetRows);
                    LogExposureClass.LOGGER.info(strFeedbackN);
                    break;
                default:
                    final String strFeedbackErr = LogExposureClass.getUnsupportedFeatures(key, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                    throw new UnsupportedOperationException(strFeedbackErr);
            }
        }

        /**
         * extends functionality for Executions
         *
         * @param strPurpose purpose of query
         * @param resultSet result-set
         * @param objProperties properties (with features to apply)
         */
        public static void digestCustomQueryProperties(final String strPurpose, final ResultSet resultSet, final Properties objProperties) {
            intColumnsIs = getResultSetNumberOfColumns(resultSet);
            for (final Object obj : objProperties.keySet()) {
                final String key = (String) obj;
                final String strFeedback = String.format("Evaluating ResultSet for %s...", key);
                LogExposureClass.LOGGER.debug(strFeedback);
                captureToLogResultsetAttributes(key, strPurpose, objProperties);
            }
        }

        /**
         * get structure from ResultSet
         *
         * @param resultSet result-set
         * @return List of Properties
         */
        public static List<Properties> getResultSetColumnStructure(final ResultSet resultSet) {
            final List<Properties> listResultSet = new ArrayList<>();
            try {
                final ResultSetMetaData metaData = resultSet.getMetaData();
                final int columnCount = metaData.getColumnCount();
                for (int columnNumber = 1; columnNumber <= columnCount; columnNumber++) {
                    final Properties colProperties = RowProcessingClass.captureMetadataIntoProperties(metaData, columnNumber);
                    listResultSet.add(colProperties);
                }
            } catch (SQLException e) {
                final String strFeedbackErr = String.format(STR_I18N_STM_UNB, "structures", e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
            return listResultSet;
        }

        /**
         * get column values from ResultSet
         *
         * @param resultSet result-set
         * @return List of Properties
         */
        public static List<Properties> getResultSetColumnValues(final ResultSet resultSet) {
            final List<Properties> listResultSet = new ArrayList<>();
            try {
                final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                final int columnCount = resultSetMetaData.getColumnCount();
                int intRow = 0;
                while (resultSet.next()) {
                    listResultSet.add(RowProcessingClass.getCurrentRowIntoProperties(resultSet, columnCount, resultSetMetaData));
                    intRow++;
                }
                intResultSetRows = intRow;
                final String strFeedback = String.format("I have found %d records", intRow);
                LogExposureClass.LOGGER.debug(strFeedback);
            } catch (SQLException e) {
                final String strFeedbackErr = String.format(STR_I18N_STM_UNB, "structures", e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
            return listResultSet;
        }

        /**
         * get column values from ResultSet
         *
         * @param resultSet result-set
         * @return List of Properties
         */
        public static List<Properties> getResultSetColumnValuesWithNullCheck(final ResultSet resultSet) {
            if (resultSet == null) {
                final String strFeedback = "ResultSet is null";
                LogExposureClass.LOGGER.error(strFeedback);
            }
            return resultSet == null ? new ArrayList<>() : getResultSetColumnValues(resultSet);
        }

        /**
         * get list of values
         *
         * @param resultSet result-set
         * @return list of strings
         */
        public static List<String> getResultSetListOfStrings(final ResultSet resultSet) {
            final List<String> listStrings = new ArrayList<>();
            try {
                while (resultSet.next()) {
                    listStrings.add(resultSet.getString(1));
                }
            } catch (SQLException e) {
                final String strFeedback = String.format(STR_I18N_STM_UNB, "list of strings", e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedback);
            }
            return listStrings;
        }

        /**
         * get # of Columns from ResultSet
         *
         * @param resultSet result-set
         * @return number of columns
         */
        private static int getResultSetNumberOfColumns(final ResultSet resultSet) {
            int intColumns = -1;
            try {
                final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                intColumns = resultSetMetaData.getColumnCount();
            } catch (SQLException e) {
                final String strFeedback = String.format(STR_I18N_STM_UNB, "columns", e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedback);
            }
            return intColumns;
        }

        /**
         * ResultSet capturing standardized
         * @param objStatement statement
         * @param rsProperties result set Properties
         * @param queryProperties properties (with features to apply)
         * @return List of Properties
         */
        public static List<Properties> getResultSetStandardized(final Statement objStatement, final Properties rsProperties, final Properties queryProperties) {
            List<Properties> listReturn = new ArrayList<>();
            final String strPurpose = rsProperties.get("Purpose").toString();
            final String strQueryToUse = rsProperties.get("QueryToUse").toString();
            final String strFetchType = rsProperties.get("FetchType").toString();
            try (ResultSet rsStandard = executeCustomQuery(objStatement, strPurpose, strQueryToUse, queryProperties)) {
                assert rsStandard != null;
                listReturn = switch (strFetchType) {
                    case "Structure" -> getResultSetColumnStructure(rsStandard);
                    case STR_VALUES -> getResultSetColumnValuesWithNullCheck(rsStandard);
                    default -> {
                        final String strFeedbackErr = LogExposureClass.getUnsupportedFeatures(strFetchType, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                        throw new UnsupportedOperationException(strFeedbackErr);
                    }
                };
            } catch (SQLException e) {
                final String strFeedback = String.format("Statement execution for %s has failed with following error: %s", strPurpose, e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedback);
            }
            return listReturn;
        }

        /**
         * Basic features for Databases
         */
        public static final class RowProcessingClass {

            /**
             * Capturing Metadata Into Properties
             * @param metaData metadata from SQL query
             * @param columnNumber number of current column
             * @return Properties
             * @throws SQLException in case of any SQL error
             */
            private static Properties captureMetadataIntoProperties(final ResultSetMetaData metaData, final int columnNumber) throws SQLException {
                final Properties colProperties = new Properties();
                colProperties.put("Display Size", metaData.getColumnDisplaySize(columnNumber));
                colProperties.put("Name", metaData.getColumnName(columnNumber));
                colProperties.put("Precision", metaData.getPrecision(columnNumber));
                colProperties.put("Scale", metaData.getScale(columnNumber));
                colProperties.put("Type", metaData.getColumnTypeName(columnNumber));
                colProperties.put("Nullable", metaData.isNullable(columnNumber));
                return colProperties;
            }

            /**
             * Collecting current row
             * @param resultSet digesting result-set
             * @param columnCount number of columns to iterate through
             * @param resultSetMetaData column names
             * @return Properties with current row value and their name
             */
            private static Properties getCurrentRowIntoProperties(final ResultSet resultSet, final int columnCount, final ResultSetMetaData resultSetMetaData) throws SQLException {
                final Properties currentRow = new Properties();
                for (int colIndex = 1; colIndex <= columnCount; colIndex++) {
                    String crtValue = resultSet.getString(colIndex);
                    if (resultSet.wasNull()) {
                        crtValue = STR_NULL;
                    }
                    currentRow.put(resultSetMetaData.getColumnName(colIndex), crtValue);
                }
                return currentRow;
            }

            /**
             * Constructor
             */
            private RowProcessingClass() {
                // intentionally blank
            }

        }

        /**
         * Constructor
         */
        private ResultSettingSubClass() {
            // intentionally blank
        }

    }

    /**
     * MySQL methods
     */
    public static final class SpecificMySqlSubClass {
        /**
         * Database MySQL
         */
        public static final String STR_DB_MYSQL = "MySQL";

        /**
         * Getting Connection Properties For MySQL from Environment variable
         * @return Properties
         */
        public static Properties getConnectionPropertiesForMySQL() {
            final Properties properties = new Properties();
            final String strEnv = "MYSQL";
            final String strEnvMySql = System.getenv(strEnv);
            if (strEnvMySql == null) {
                final String strFeedback = String.format("Environment variable %s not found!", strEnv);
                LogExposureClass.LOGGER.error(strFeedback);
            } else {
                final String strFeedback = String.format("Environment variable %s was found successfully!", strEnv);
                LogExposureClass.LOGGER.debug(strFeedback);
                final InputStream inputStream = new ByteArrayInputStream(strEnvMySql.getBytes(Charset.defaultCharset()));
                final JsonNode ndMySQL = JsonOperationsClass.getJsonFileNodes(inputStream);
                properties.put("ServerName", JsonOperationsClass.getJsonValue(ndMySQL, "/ServerName"));
                properties.put("Port", JsonOperationsClass.getJsonValue(ndMySQL, "/Port"));
                properties.put("Username", JsonOperationsClass.getJsonValue(ndMySQL, "/Username"));
                properties.put("Password", JsonOperationsClass.getJsonValue(ndMySQL, "/Password"));
                properties.put("ServerTimezone", JsonOperationsClass.getJsonValue(ndMySQL, "/ServerTimezone"));
            }
            return properties;
        }

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
            final String strQueryToUse = getPreDefinedQuery(STR_DB_MYSQL, strWhich);
            final Properties rsProperties = packageResultSetProperties(strPurpose, strQueryToUse, strFetchType);
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
                    final List<Properties> listProps = getMySqlPreDefinedInformation(objStatement, strWhich, "purpose " + strWhich, STR_VALUES);
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
        private SpecificMySqlSubClass() {
            // intentionally blank
        }
    }

    /**
     * SQLite methods
     */
    public static final class SpecificSqLiteSubClass {
        /**
         * Internal database variable
         */
        private static String internalDatabase;

        /**
         * Getter for internalDatabase
         */
        public static String getInternalDatabase() {
            return internalDatabase;
        }

        /**
         * Initiates a SQLite connection
         * @return Connection
         */
        public static Connection getSqLiteConnection() {
            return getSqLiteConnection(internalDatabase);
        }

        /**
         * Initiates a SQLite connection
         * 
         * @param strSqLiteFile file with SQLite database
         * @return Connection
         */
        public static Connection getSqLiteConnection(final String strSqLiteFile) {
            final String strConnection = "jdbc:sqlite:" + strSqLiteFile.replace("\\", "/");
            final String strFeedbackAtmpt = String.format("Will attempt to create a %s connection to database %s using %s as connection string", BasicStructuresClass.STR_SQLITE, strSqLiteFile, strConnection);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            Connection connection = null;
            try {
                connection = DriverManager.getConnection(strConnection);
                final String strFeedbackOk = String.format("%s connection to database %s was successfully established!", BasicStructuresClass.STR_SQLITE, strSqLiteFile);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
                Function.create(connection, "REGEXP_LIKE", new Function() {
                    @Override
                    protected void xFunc() throws SQLException {
                        final String text = value_text(0);
                        final String pattern = value_text(1);
                        result(RegularExpressionsClass.doesExist(text, pattern));
                    }
                });
                Function.create(connection, "REGEXP_REPLACE", new Function() {
                    @Override
                    protected void xFunc() throws SQLException {
                        final String text = value_text(0);
                        final String pattern = value_text(1);
                        final String replacement = value_text(2);
                        result(Pattern.compile(pattern).matcher(text).replaceAll(replacement));
                    }
                });
            } catch(SQLException e) {
                final String strFeedbackErr = String.format("%s connection has failed %s", BasicStructuresClass.STR_SQLITE, e.getLocalizedMessage());
                LogExposureClass.LOGGER.debug(strFeedbackErr);
            }
            return connection;
        }

        /**
         * Get result-set from SQLite
         * @param strQueryPurpose Query purpose
         * @param strQuery Query to execute
         * @return List of Values
         */
        public static List<Properties> getSqLiteResultSetValues(final String strQueryPurpose, final String strQuery) {
            final Properties objProperties = new Properties();
            List<Properties> listReturn = new ArrayList<>();
            try (Connection objConnection = getSqLiteConnection(internalDatabase)) {
                assert objConnection != null;
                try (Statement objStatement = ConnectivitySubClass.createSqlStatement(BasicStructuresClass.STR_SQLITE, objConnection);
                    ResultSet rsCols = executeCustomQuery(objStatement, strQueryPurpose, strQuery, objProperties)) {
                    assert rsCols != null;
                    listReturn = ResultSettingSubClass.getResultSetColumnValues(rsCols);
                }
            } catch(SQLException e){
                final String strFeedback = String.format("%s connection has failed at %s: %s", BasicStructuresClass.STR_SQLITE, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)), e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedback);
            }
            return listReturn;
        }

        /**
         * Setter for internalDatabase
         */
        public static void setInternalDatabase(final String inDatabase) {
            internalDatabase = inDatabase;
        }

        /**
         * constructor
         */
        private SpecificSqLiteSubClass() {
            // empty constructor
        }

        /**
         * List and Maps management
         */
        public static final class SqLiteStatisticsSubClass {

            /**
             * Build Information Box
             * @return String
             */
            public static String buildSqLiteFileInfoBox() {
                final Path fileName = Path.of(internalDatabase);
                return HtmlClass.buildFileInfoBox(fileName);
            }

            /**
             * read SQLite tables and their record count
             * @return StringBuilder
             */
            private static StringBuilder buildTableRecordCounting() {
                final String strQueryCount = getPreDefinedQuery(BasicStructuresClass.STR_SQLITE, "StatisticsTableRecordCounting");
                final StringBuilder strQueryRaw = new StringBuilder(1000);
                final List<Properties> resultTables = getTablesAndTheirSequence();
                resultTables.forEach(objProperty -> {
                    if (!strQueryRaw.isEmpty()) {
                        strQueryRaw.append(" UNION ALL ");
                    }
                    strQueryRaw.append(String.format(strQueryCount,
                            objProperty.get(BasicStructuresClass.STR_TABLE),
                            objProperty.get("Sequence"),
                            objProperty.get(BasicStructuresClass.STR_TABLE)));
                });
                return strQueryRaw;
            }

            /**
             * read SQLite tables and their sequence
             * @return List<Properties>
             */
            private static List<Properties> getTablesAndTheirSequence() {
                final String queryTables = getPreDefinedQuery(BasicStructuresClass.STR_SQLITE, "StatisticsTablesAndTheirSequence");
                final String strFeedback = String.format("Table list and their sequence query is: %s", queryTables);
                LogExposureClass.LOGGER.debug(strFeedback);
                return getSqLiteResultSetValues("Table list and their sequence", queryTables);
            }

            /**
             * Outputs table statistics into an HTML table
             * @return String
             */
            public static String getTableStatisticsAsHtmlTable() {
                final StringBuilder queryRecordCount = buildTableRecordCounting();
                final String queryTableStats = getPreDefinedQuery(BasicStructuresClass.STR_SQLITE, "StatisticsTables");
                final String strFinalQuery =  String.format(queryTableStats, queryRecordCount);
                final List<Properties> resultTableStats = getSqLiteResultSetValues("Table Statistics", strFinalQuery);
                final List<String> desiredOrder = List.of("#", BasicStructuresClass.STR_TABLE, "Records", "Sequence", "Gap");
                final List<SequencedMap<Object, Object>> orderedList = resultTableStats.stream()
                        .map(prop -> BasicStructuresClass.ListAndMapSubClass.sortProperties(prop, desiredOrder))
                        .toList();
                return HtmlClass.TableSubClass.getListOfSequencedMapIntoHtmlTable(orderedList, new Properties());
            }

            /**
             * constructor
             */
            private SqLiteStatisticsSubClass() {
                // intentionally left blank
            }

        }

    }

    /**
     * Snowflake methods
     */
    public static final class SpecificSnowflakeSubClass {
        /**
         * String for Snowflake
         */
        public static final String STR_SNOWFLAKE = "Snowflake";
        /**
         * standard String
         */
        public static final String STR_ROLES = "Roles";
        /**
         * standard String
         */
        private static String strUserName;

        /**
         * Snowflake Bootstrap
         *
         * @param objStatement statement
         */
        public static void executeSnowflakeBootstrapQuery(final Statement objStatement) {
            final String strQueryToUse = "ALTER SESSION SET JDBC_QUERY_RESULT_FORMAT='JSON';";
            executeQueryWithoutResultSet(objStatement, "Bootstrap", strQueryToUse);
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
            } catch(SQLException e) {
                final String strFeedbackErr = String.format("%s connection has failed %s", STR_SNOWFLAKE, e.getLocalizedMessage());
                LogExposureClass.LOGGER.debug(strFeedbackErr);
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
            final String strQueryToUse = getPreDefinedQuery(STR_SNOWFLAKE, strAction);
            final Properties rsProperties = packageResultSetProperties("purpose: " + strAction, strQueryToUse, strFetchType);
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
            String currentUser = strUserName;
            if (strUserName == null) {
                currentUser = ShellingClass.getCurrentUserAccount().toUpperCase(Locale.getDefault());
            }
            if (currentUser.isEmpty()) {
                currentUser = "UNKNOWN_USER";
            }
            properties.put("user", currentUser);
            properties.put("db", strDatabase);
            properties.put("authenticator", propInstance.get("Authenticator").toString().replace("\"", ""));
            properties.put("role", propInstance.get("Role").toString().replace("\"", ""));
            properties.put("schema", propInstance.get("Schema").toString().replace("\"", ""));
            properties.put("warehouse", propInstance.get("Warehouse").toString().replace("\"", ""));
            properties.put("tracing", "SEVERE"); // to hide INFO and Warnings which are visible otherwise
            return properties;
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
                    getSnowflakePreDefinedInformation(objStatement, strAction, STR_VALUES);
                }
            } catch(SQLException e) {
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
        private SpecificSnowflakeSubClass() {
            // intentionally blank
        }
    }

    /**
     * Constructor
     */
    private DatabaseOperationsClass() {
        // intentionally blank
    }
}
