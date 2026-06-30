/**
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.cli;

import java.time.LocalDateTime;
import java.time.ZoneId;

import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.core.ProjectClass;
import io.github.dgp_eu.tools.core.TimingClass;
import org.apache.maven.model.Model;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Common class for Interactive service
 */
public final class CommonInteractiveClass {
    /** Exit Code variable */
    private static int exitCode;
    /** Start Date Time variable */
    private static LocalDateTime startDateTime;
    /** Picocli attribute for Commands */
    public static final String PICOCLI_CMNDS = "%nCommands:%n%n";
    /** Picocli attribute for Description */
    public static final String PICOCLI_DESCR = "%nDescription:%n%n";
    /** Picocli attribute for Options */
    public static final String PICOCLI_OPTNS = "%nOptions:%n%n";
    /** Picocli attribute for Parameters */
    public static final String PICOCLI_PRMTRS = "%nParameters:%n%n";
    /** Picocli attribute for Usage */
    public static final String PICOCLI_USAGE = "%nUsage:%n%n";

    /**
     * Shut Down sequence
     * @param inOperation main Operation executed
     */
    private static void shutMeDownLight(final String inOperation) {
        final String strFeedbackExit = String.format("Exiting with code %s", exitCode);
        LogExposureClass.LOGGER.info(strFeedbackExit);
        final LocalDateTime finishTimeStamp = LocalDateTime.now(ZoneId.systemDefault());
        final String strFeedbackEnd = TimingClass.logDuration(startDateTime,
                finishTimeStamp,
                String.format("Entire operation %s completed", inOperation));
        LogExposureClass.LOGGER.info(strFeedbackEnd);
    }

    /**
     * Shut Down sequence
     * @param inExitCode input Exit code
     * @param inOperation main Operation executed
     */
    public static void shutMeDownWithParameters(final int inExitCode, final String inOperation) {
        setExitCode(inExitCode);
        shutMeDownLight(inOperation);
    }

    /**
     * Starting sequence
     */
    private static void startMeUp() {
        final String strFeedbackLines = "-".repeat(80);
        LogExposureClass.LOGGER.info(strFeedbackLines);
        final String[] prjProperties = getProjectProperties();
        final String strFeedback = String.format("%s:%s v.%s => New Execution", prjProperties[0], prjProperties[1], prjProperties[2]);
        LogExposureClass.LOGGER.info(strFeedback);
        LogExposureClass.LOGGER.info(strFeedbackLines);
    }

    /**
     * Starting sequence with parameters
     * @param inLogFile input log file
     * @param inPomFile input Project Object Model file
     */
    public static void startMeUpWithParameters(final String inLogFile, final String inPomFile) {
        setStartDateTime();
        LogExposureClass.ConfigurationSubClass.initiate(inLogFile);
        ProjectClass.setPomFile(inPomFile);
        startMeUp();
    }

    /**
     * Get Project Properties
     * @return String array with GroupId, ArtifactId and Version
     */
    private static String[] getProjectProperties() {
        final String[] strToReturn = new String[3];
        strToReturn[0] = ProjectClass.getProjectGroupId();
        final Model projectModel = ProjectClass.getProjectModel();
        strToReturn[1] = projectModel.getArtifactId();
        strToReturn[2] = ProjectClass.getProjectVersion();
        return strToReturn;
    }

    /**
     * Getter for Exit Code
     */
    public static int getExitCode() {
        return exitCode;
    }

    /**
     * Setter for Exit Code
     * @param inExitCode actual Exit Code
     */
    public static void setExitCode(final int inExitCode) {
        exitCode = inExitCode;
    }

    /**
     * Setter for Start DateTime
     */
    public static void setStartDateTime() {
        startDateTime = LocalDateTime.now(ZoneId.systemDefault());
    }

    /**
     * Reusable Folder Destination for Picocli logic
     */
    @Command(synopsisHeading      = PICOCLI_USAGE,
             descriptionHeading   = PICOCLI_DESCR,
             parameterListHeading = PICOCLI_PRMTRS,
             optionListHeading    = PICOCLI_OPTNS,
             commandListHeading   = PICOCLI_CMNDS)
    /* default */ public static class FolderDestinationOptionMixinClass {

        /**
         * String for FolderName
         */
        @Option(
                names = {"-fldDst", "--folderDestination"},
                description = "Destination Folder where archives will be created (required, only one)",
                arity = BasicStructuresClass.ARITY_ONLY_ONE,
                required = true)
        private String strDestFolder;

        /**
         * Getter for strDestFolder
         * @return String of Folder Names (1)
         */
        public String getFolderDestination() {
            return strDestFolder;
        }

    }

    /**
     * Reusable Folder Name for Picocli logic
     */
    @Command(synopsisHeading      = PICOCLI_USAGE,
             descriptionHeading   = PICOCLI_DESCR,
             parameterListHeading = PICOCLI_PRMTRS,
             optionListHeading    = PICOCLI_OPTNS,
             commandListHeading   = PICOCLI_CMNDS)
    /* default */ public static class FolderNameOptionMixinClass {

        /**
         * String for FolderName
         */
        @Option(
                names = {"-fldNm", "--folderName"},
                description = "Folder Name in scope (required, one or more)",
                arity = BasicStructuresClass.ARITY_ONE_OR_MORE,
                required = true)
        private String[] strFolderNames;

        /**
         * Getter for strFolderNames
         * @return array of Folder Names (1 or many)
         */
        public String[] getFolderNames() {
            return strFolderNames.clone();
        }

    }

    /**
     * Reusable input File Name for Picocli logic
     */
    @Command(synopsisHeading      = PICOCLI_USAGE,
             descriptionHeading   = PICOCLI_DESCR,
             parameterListHeading = PICOCLI_PRMTRS,
             optionListHeading    = PICOCLI_OPTNS,
             commandListHeading   = PICOCLI_CMNDS)
    /* default */ public static class InFileNameOptionMixinClass {

        /**
         * String for in FileNames
         */
        @CommandLine.Option(
                names = {"-if", "--inFileName"},
                description = "Input file(s) to consider",
                arity = BasicStructuresClass.ARITY_ONE_OR_MORE,
                required = true)
        private String[] strInFileNames;

        /**
         * Getter for strFileNames
         * @return array of File Names (1 or many)
         */
        public String[] getInFileNames() {
            return strInFileNames.clone();
        }

    }

    /**
     * Reusable local DB File Name
     */
    @Command(synopsisHeading      = PICOCLI_USAGE,
             descriptionHeading   = PICOCLI_DESCR,
             parameterListHeading = PICOCLI_PRMTRS,
             optionListHeading    = PICOCLI_OPTNS,
             commandListHeading   = PICOCLI_CMNDS)
    /* default */ public static class LocalDatabaseFileMixinClass {

        /**
         * String for out FileName
         */
        @CommandLine.Option(
                names = {"-ldb", "--localDatabaseFile"},
                description = "Local Database File",
                arity = BasicStructuresClass.ARITY_ONLY_ONE,
                required = true)
        private String strLocalDbFile;

        /**
         * Getter for strCsvFileName
         * @return array of CSV File Name (only 1, required)
         */
        public String getLocalDbFile() {
            return strLocalDbFile;
        }

    }

    /**
     * Reusable output File Name for Picocli logic
     */
    @Command(synopsisHeading      = PICOCLI_USAGE,
             descriptionHeading   = PICOCLI_DESCR,
             parameterListHeading = PICOCLI_PRMTRS,
             optionListHeading    = PICOCLI_OPTNS,
             commandListHeading   = PICOCLI_CMNDS)
    /* default */ public static class OutFileNameOptionMixinClass {

        /**
         * String for out FileName
         */
        @CommandLine.Option(
                names = {"-of", "--outFileName"},
                description = "Destination file to write information into",
                arity = BasicStructuresClass.ARITY_ONLY_ONE,
                required = true)
        private String strOutFileName;

        /**
         * Getter for strCsvFileName
         * @return array of CSV File Name (only 1, required)
         */
        public String getOutFileName() {
            return strOutFileName;
        }

    }

    /**
     * Reusable port for Picocli logic
     */
    @Command(synopsisHeading      = PICOCLI_USAGE,
             descriptionHeading   = PICOCLI_DESCR,
             parameterListHeading = PICOCLI_PRMTRS,
             optionListHeading    = PICOCLI_OPTNS,
             commandListHeading   = PICOCLI_CMNDS)
    /* default */ public static class PortOptionMixinClass {

        /**
         * String for out FileName
         */
        @CommandLine.Option(
                names = {"-p", "--port"},
                description = "Port Number for web user interface",
                arity = BasicStructuresClass.ARITY_ONLY_ONE,
                required = true)
        private long portNumber;

        /**
         * Getter for portNumber
         * @return integer number for Port number (only 1, required)
         */
        public long getPortNumber() {
            return portNumber;
        }

    }

    /**
     * Constructor
     */
    private CommonInteractiveClass() {
        // intentionally blank
    }

}
