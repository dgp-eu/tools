/*
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.database.demo;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import io.github.dgp_eu.tools.cli.CommonInteractiveClass;
import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.databases.SpecificMySqlClass;
import io.github.dgp_eu.tools.databases.SpecificSnowflakeClass;
import io.github.dgp_eu.tools.json.JsonOperationsClass;
import picocli.CommandLine;
import tools.jackson.databind.JsonNode;

/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
            GetInformationFromDatabase.class
    }
)
public final class ApplicationDatabases {

    /**
     * Constructor
     *
     * @param args command-line arguments
     */
    /* default */ static void main(final String... args) {
        CommonInteractiveClass.startMeUpWithParameters("logs/DGP-EU_Tools-Databases-", "/tools-databases-pom.xml");
        final int intDbExitCode = new CommandLine(new ApplicationDatabases()).execute(args);
        CommonInteractiveClass.shutMeDownWithParameters(intDbExitCode, args[0]);
    }

    /** Constructor */
    private ApplicationDatabases() {
        super();
    }

}


/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "GetInformationFromDatabase",
                     description = "Gets information from Database into Log file")
class GetInformationFromDatabase implements Runnable {

    /**
     * Known Database Types
     */
	/* default */ static final List<String> LST_DB_TYPES = Arrays.asList(
        "MySQL",
        "Snowflake"
    );

    /**
     * Known Information Types
     */
	/* default */ static final List<String> LST_INFO_TYPES = Arrays.asList(
        "Columns",
        "Databases",
        "Schemas",
        "TablesAndViews",
        "Views",
        "ViewsLight"
    );

    /**
     * String for Database Type
     */
    @CommandLine.Option(
        names = { "-dbTp", "--databaseType" },
        description = "Type of Database",
        arity = BasicStructuresClass.ARITY_ONLY_ONE,
        required = true,
        completionCandidates = DatabaseTypes.class)
    private String strDbType;

    /**
     * String for Information Type
     */
    @CommandLine.Option(
        names = { "-infTp", "--informationType" },
        description = "Type of Information",
        arity = BasicStructuresClass.ARITY_ONE_OR_MORE,
        required = true,
        completionCandidates = InfoTypes.class)
    private String strInfoType;

    /**
     * Listing available options
     */
    /* default */ static class DatabaseTypes implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            return LST_DB_TYPES.iterator();
        }
    }

    /**
     * Listing available options
     */
    /* default */ static class InfoTypes implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            return LST_INFO_TYPES.iterator();
        }
    }

    private static Properties getEnvironmentVariableValueForMySql() {
        final InputStream inputStream = BasicStructuresClass.getEnvironmentVariableIntoInputStream("MYSQL");
        final JsonNode ndMySQL = JsonOperationsClass.getJsonFileNodes(inputStream);
        final Properties properties = new Properties();
        properties.put("ServerName", JsonOperationsClass.getJsonValue(ndMySQL, "/ServerName"));
        properties.put("Port", JsonOperationsClass.getJsonValue(ndMySQL, "/Port"));
        properties.put("Username", JsonOperationsClass.getJsonValue(ndMySQL, "/Username"));
        properties.put("Password", JsonOperationsClass.getJsonValue(ndMySQL, "/Password"));
        properties.put("ServerTimezone", JsonOperationsClass.getJsonValue(ndMySQL, "/ServerTimezone"));
        return properties;
    }

    /**
     * Action logic
     *
     * @param strDatabaseType type of Database (predefined values)
     */
    private static void performAction(final String strDatabaseType, final String strLclInfoType) {
        Properties properties = new Properties();
        switch (strDatabaseType) {
            case "MySQL":
                properties = getEnvironmentVariableValueForMySql();
                SpecificMySqlClass.performMySqlPreDefinedAction(strLclInfoType, properties);
                break;
            case "Snowflake":
                SpecificSnowflakeClass.performSnowflakePreDefinedAction(strLclInfoType, properties);
                break;
            default:
                final String strFeedback = String.format("Unknown %s argument received in %s, do not know what to do with it, therefore will quit, bye!", strDatabaseType, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                LogExposureClass.LOGGER.error(strFeedback);
                break;
        }
    }

    @Override
    public void run() {
        if (!LST_DB_TYPES.contains(strDbType)) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Invalid value for --databaseType: " + strDbType + ". Valid values are: " + LST_DB_TYPES
            );
        }
        if (!LST_INFO_TYPES.contains(strInfoType)) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Invalid value for --informationType: " + strInfoType + ". Valid values are: " + LST_INFO_TYPES
            );
        }
        performAction(strDbType, strInfoType);
    }

    /**
     * Constructor
     */
    protected GetInformationFromDatabase() {
        super();
    }
}
