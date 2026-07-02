/** Copyright 2026 Daniel-Gheorghe Popiniuc */
package io.github.dgp_eu.tools.databases;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sqlite.Function;

import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.FileOperationsClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.core.RegularExpressionsClass;
import io.github.dgp_eu.tools.databases.DatabaseOperationsClass.ConnectivitySubClass;
import io.github.dgp_eu.tools.databases.DatabaseOperationsClass.ResultSettingSubClass;

/**
 * SQLite methods
 */
public final class SpecificSqLiteClass {
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
     * Checks for valid path, file existence and readability, readability, extension and format
     * @param inDatabase input SQLite file as String
     */
    public static void checkDatabaseFile(final String inDatabase) {
        final long fileSize = FileOperationsClass.RetrievingSubClass.getFileSizeIfFileExistsAndIsReadable(inDatabase);
        if (fileSize > 0) {
            final Path databasePath = Path.of(inDatabase).toAbsolutePath().normalize();
            SpecificSqLiteClass.checkDatabaseFileExtension(databasePath);
            SpecificSqLiteClass.checkDatabaseFileFormat(databasePath);
        }
    }

    /**
     * Checks if SQLite database has expected file extension
     * @param databasePath input SQLite file as Path
     */
    private static void checkDatabaseFileExtension(final Path databasePath) {
        final String fileName = databasePath.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!fileName.endsWith(".db")
                && !fileName.endsWith(".sqlite")) {
            throw new IllegalArgumentException("Database file must have .db or .sqlite extension");
        }
    }

    /**
     * Checks if SQLite database is a valid SQLite Format 3
     * @param databasePath input SQLite file as Path
     */
    private static void checkDatabaseFileFormat(final Path databasePath) {
        try (InputStream inputStream = Files.newInputStream(databasePath)) {
            final byte[] header = inputStream.readNBytes(16);
            final String headerStr = new String(header, StandardCharsets.US_ASCII);
            if (!headerStr.startsWith("SQLite format 3")) {
                throw new IllegalArgumentException("Database file is not a valid SQLite database");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read database file", e);
	
        }
    }

    /**
     * Initiates a SQLite connection
     * 
     * @param strSqLiteFile file with SQLite database
     * @return Connection
     */
    public static Connection getSqLiteConnection() {
        final String strConnection = "jdbc:sqlite:" + internalDatabase.replace("\\", "/");
        final String strFeedbackAtmpt = String.format("Will attempt to create a %s connection to database %s using %s as connection string", BasicStructuresClass.STR_SQLITE, internalDatabase, strConnection);
        LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(strConnection);
            final String strFeedbackOk = String.format("%s connection to database %s was successfully established!", BasicStructuresClass.STR_SQLITE, internalDatabase);
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
        try (Connection objConnection = getSqLiteConnection()) {
            assert objConnection != null;
            try (Statement objStatement = ConnectivitySubClass.createSqlStatement(BasicStructuresClass.STR_SQLITE, objConnection);
                ResultSet rsCols = DatabaseOperationsClass.executeCustomQuery(objStatement, strQueryPurpose, strQuery, objProperties)) {
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
     * Implementation for speed optimization sequence
     * @param objStatement Statement
     */
    public static void runSequenceSpeedOptimizationForWriting(final Statement objStatement) {
        final SequencedMap<String, String> sequenceMap = Stream.of(
                Map.entry("Busy Timeout", "PRAGMA busy_timeout = 3600;"),
                Map.entry("No Journal", "PRAGMA journal_mode = OFF;"),
                Map.entry("No Synchronous", "PRAGMA synchronous = 0;"),
                Map.entry("Large Cache Size", "PRAGMA cache_size = 1000000;"),
                Map.entry("Memory Operation", "PRAGMA temp_store = MEMORY;"),
                Map.entry("Large Page Size", "PRAGMA page_size = 16384;")
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, _) -> e1, LinkedHashMap::new));
        sequenceMap.forEach((strLabel, strQuery) -> {
            final String strFeedback = String.format("Query labeled \"%s\" has following definition: \"%s\"", strLabel, strQuery);
            LogExposureClass.LOGGER.debug(strFeedback);
            DatabaseOperationsClass.executeQueryWithoutResultSet(objStatement, strLabel, strQuery);
        });
    }

    /**
     * Setter for internalDatabase
     */
    public static void setInternalDatabase(final String inDatabase) {
        checkDatabaseFile(inDatabase);
        internalDatabase = inDatabase;
    }

    /**
     * constructor
     */
    private SpecificSqLiteClass() {
        // empty constructor
    }

    /**
     * List and Maps management
     */
    public static final class SqLiteStatisticsSubClass {

        /**
         * read SQLite tables and their record count
         * @return StringBuilder
         */
        private static StringBuilder buildTableRecordCounting() {
            final String strQueryCount = DatabaseOperationsClass.getPreDefinedQuery(BasicStructuresClass.STR_SQLITE, "StatisticsTableRecordCounting");
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
            final String queryTables = DatabaseOperationsClass.getPreDefinedQuery(BasicStructuresClass.STR_SQLITE, "StatisticsTablesAndTheirSequence");
            final String strFeedback = String.format("Table list and their sequence query is: %s", queryTables);
            LogExposureClass.LOGGER.debug(strFeedback);
            return getSqLiteResultSetValues("Table list and their sequence", queryTables);
        }

        /**
         * Outputs table statistics into an HTML table
         * @return String
         */
        public static List<SequencedMap<Object, Object>> getTableStatisticsIntoListForHtmlTable() {
            final StringBuilder queryRecordCount = buildTableRecordCounting();
            final String queryTableStats = DatabaseOperationsClass.getPreDefinedQuery(BasicStructuresClass.STR_SQLITE, "StatisticsTables");
            final String strFinalQuery =  String.format(queryTableStats, queryRecordCount);
            final List<Properties> resultTableStats = getSqLiteResultSetValues("Table Statistics", strFinalQuery);
            final List<String> desiredOrder = List.of("#", BasicStructuresClass.STR_TABLE, "Records", "Sequence", "Gap");
            return resultTableStats.stream()
                    .map(prop -> BasicStructuresClass.ListAndMapSubClass.sortProperties(prop, desiredOrder))
                    .toList();
        }

        /**
         * constructor
         */
        private SqLiteStatisticsSubClass() {
            // intentionally left blank
        }

    }

}