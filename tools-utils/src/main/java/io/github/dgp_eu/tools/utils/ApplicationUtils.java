/** Copyright 2026 Daniel-Gheorghe Popiniuc */
package io.github.dgp_eu.tools.utils;

import java.nio.file.Path;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.github.dgp_eu.tools.cli.CommonInteractiveClass;
import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.FileOperationsClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.core.ProjectClass;
import io.github.dgp_eu.tools.core.ShellingClass;
import io.github.dgp_eu.tools.core.TimingClass;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
            AnalyzeColumnsFromCsvFiles.class,
            AnalyzePomFiles.class,
            ArchiveFolders.class,
            CalculateSunriseAndSunset.class,
            CaptureChecksumsOfFilesFromFoldersIntoCsvFile.class,
            CaptureImportsFromJavaSourceFilesIntoCsvFile.class,
            CaptureWindowsApplicationsInstalledIntoCsvFile.class,
            CleanOlderFilesFromFolder.class,
            GetSubFoldersFromFolders.class
    }
)
public class ApplicationUtils {

    /**
     * Application logic
     * @param args input arguments
     */
    public static void main( final String[] args ) {
        CommonInteractiveClass.startMeUpWithParameters("logs/DGP-EU_Tools-Utils-", "/tools-utils-pom.xml");
        final int intUtilsExitCode = new CommandLine(new ApplicationUtils()).execute(args);
        CommonInteractiveClass.shutMeDownWithParameters(intUtilsExitCode, args[0]);
    }

}

/**
 * Captures execution environment details into Log file
 */
@CommandLine.Command(name = "AnalyzeColumnsFromCsvFiles",
                     description = "Analyze columns from CSV file")
class AnalyzeColumnsFromCsvFiles implements Runnable {
    /**
     * adds the options defined in 
     * CommonInteractiveClass.FileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.InFileNameOptionMixinClass optFileNames = new CommonInteractiveClass.InFileNameOptionMixinClass();

    /**
     *
     * @param strFileName input File
     * @param intColToEval number of column to evaluate (starting from 0)
     * @param intColToGrpBy number of column to group by (starting from 0)
     */
    private static void storeWordFrequencyIntoCsvFile(final String strFileName,
                                                      final Integer intColToEval,
                                                      final Integer intColToGrpBy) {
        // Group values by category
        final Map<String, List<String>> groupedColumns = FileOperationsClass.ContentReadingSubClass.getListOfValuesFromColumnGroupedByAnotherColumnValuesFromCsvFile(strFileName, intColToEval, intColToGrpBy);
        // Define merge rules
        final Map<List<String>, String> mergeRules = Map.of(
                List.of("ARRAY", "OBJECT", "VARIANT"), "COMPOSITE__STRUCTURED",
                List.of("FLOAT", "NUMBER"), "COMPOSITE__NUMERIC",
                List.of("DATETIME", "TIMESTAMP", "TIMESTAMP_LTZ", "TIMESTAMP_NTZ", "TIMESTAMP_TZ"), "COMPOSITE__TIMESTAMP",
                List.of("BINARY", "TEXT", "VARCHAR"), "COMPOSITE__TEXT"
        );
        final Map<String, List<String>> grpCols = BasicStructuresClass.ListAndMapSubClass.mergeKeys(groupedColumns, mergeRules);
        final String strFeedback = "=".repeat(20) + strFileName + "=".repeat(20);
        LogExposureClass.LOGGER.info(strFeedback);
        FileOperationsClass.ContentWritingSubClass.setCsvColumnSeparator(',');
        grpCols.forEach((keyDataType, valList) -> {
            final String strColFileName = strFileName.replace(".csv", "__columns.csv");
            final String strFeedbackFile = "Writing file " + strColFileName;
            LogExposureClass.LOGGER.info(strFeedbackFile);
            FileOperationsClass.ContentWritingSubClass.setCsvLinePrefix(keyDataType);
            FileOperationsClass.ContentWritingSubClass.writeStringListToCsvFile(strColFileName, "DataType,Column", valList);
            final String strFeedbackWrt = String.format("Writing file for %s which has %s values", keyDataType, valList.size());
            LogExposureClass.LOGGER.info(strFeedbackWrt);
            final Map<String, Long> sorted = BasicStructuresClass.ListAndMapSubClass.getWordCounts(valList, "(_| )");
            FileOperationsClass.ContentWritingSubClass.writeLinkedHashMapToCsvFile(strFileName.replace(".csv", "__words.csv"), "DataType,Word,Occurrences", sorted);
        });
    }

    @Override
    public void run() {
        final String[] inFiles = optFileNames.getInFileNames();
        for (final String strFileName : inFiles) {
            storeWordFrequencyIntoCsvFile(strFileName, 3, 4);
        }
    }

    /**
     * Constructor
     */
    protected AnalyzeColumnsFromCsvFiles() {
        // intentionally blank
    }

}

/**
 * Captures sub-folder from a Given Folder into Log file
 */
@CommandLine.Command(name = "AnalyzePomFiles",
                     description = "Exposes information from one or multiple Project Object Model (Apache Maven configuration file)")
class AnalyzePomFiles implements Runnable {
    /**
     * adds the options defined in 
     * CommonInteractiveClass.FileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.InFileNameOptionMixinClass optFileNames = new CommonInteractiveClass.InFileNameOptionMixinClass();

    @Override
    public void run() {
        final String strFeedbackThis = String.format("For this project relevant POM information is: {%s}", ProjectClass.ApplicationSubClass.getApplicationDetails());
        LogExposureClass.LOGGER.info(strFeedbackThis);
        final String[] inFiles = optFileNames.getInFileNames();
        for (final String strFileName : inFiles) {
            ProjectClass.setPomFile(strFileName);
            ProjectClass.loadProjectModel();
            final String strFeedback = String.format("For given POM file %s relevant information is: {%s}", strFileName, ProjectClass.ApplicationSubClass.getApplicationDetails());
            LogExposureClass.LOGGER.info(strFeedback);
        }
    }

    /**
     * Private constructor to prevent instantiation
     */
    protected AnalyzePomFiles() {
        super();
    }

}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "ArchiveFolders",
                     description = "Archive sub-folders from a given folder")
class ArchiveFolders implements Runnable {

    /**
     * String for FolderName
     */
    @CommandLine.Option(
            names = {"-aExe", "--archivingExecutable"},
            description = "Archiving executable (including full path, required, only one)",
            arity = BasicStructuresClass.ARITY_ONLY_ONE,
            required = true)
    private String strArchivingExec;

    /**
     * String for archive name prefix
     */
    @CommandLine.Option(
            names = {"-pwd", "--archivePassword"},
            description = "Password for archive encryption (optional, only one)",
            arity = BasicStructuresClass.ARITY_ONLY_ONE)
    private String strArchivePwd;

    /**
     * String for archive name prefix
     */
    @CommandLine.Option(
            names = {"-ap", "--archivePrefix"},
            description = "Prefix to apply to archive name (optional, only one)",
            arity = BasicStructuresClass.ARITY_ONLY_ONE)
    private String strArchivePrefix;

    /**
     * String for archive name prefix
     */
    @CommandLine.Option(
            names = {"-as", "--archiveSuffix"},
            description = "Suffix to apply to archive name (optional, only one)",
            arity = BasicStructuresClass.ARITY_ONLY_ONE)
    private String strArchiveSuffix;

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass optFolderNames = new CommonInteractiveClass.FolderNameOptionMixinClass();

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderDestinationOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderDestinationOptionMixinClass optFolderDest = new CommonInteractiveClass.FolderDestinationOptionMixinClass();

    @Override
    public void run() {
        final Properties propFolder = new Properties();
        if (strArchivingExec != null) {
            final String strFeedback = String.format("Archiving executable has been set as %s", strArchivingExec);
            LogExposureClass.LOGGER.info(strFeedback);
            ArchivingClass.setArchivingExecutable(strArchivingExec);
        }
        ArchivingClass.setArchivePrefix(strArchivePrefix);
        ArchivingClass.setArchiveSuffix(strArchiveSuffix);
        if (strArchivePwd != null) {
            ArchivingClass.setArchivePwd(strArchivePwd);
        }
        final String[] inFolders = optFolderNames.getFolderNames();
        for (final String strFolder : inFolders) {
            final String strFeedback = String.format("Processing folder %s", strFolder);
            LogExposureClass.LOGGER.info(strFeedback);
            propFolder.clear();
            final Properties folderProps = FileOperationsClass.StatisticsSubClass.getFolderStatisticsRecursive(strFolder, propFolder);
            final String strFeedback2 = String.format("Initial folder statistics are %s", folderProps);
            LogExposureClass.LOGGER.info(strFeedback2);
            ArchivingClass.setArchivingDir(strFolder);
            ArchivingClass.setArchiveNameWithinDestinationFolder(optFolderDest.getFolderDestination());
            ArchivingClass.archiveFolderAs7z();
            ArchivingClass.exposeArchivedStatistics(folderProps);
        }
    }

    /**
     * Constructor
     */
    protected ArchiveFolders() {
        super();
    }
}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "CalculateSunriseAndSunset",
                     description = "Calculates Sunrise and Sunset for one or more location")
class CalculateSunriseAndSunset implements Runnable {

    /**
     * option for Longitude
     */
    @CommandLine.Option(
            names = {"-lon", "--longitude"},
            description = "Longitude",
            arity = BasicStructuresClass.ARITY_ONE_OR_MORE,
            required = true)
    private double[] dblLongitude;

    /**
     * option for Latitude
     */
    @CommandLine.Option(
            names = {"-lat", "--latitude"},
            description = "Latitude",
            arity = BasicStructuresClass.ARITY_ONE_OR_MORE,
            required = true)
    private double[] dblLatitude;

    /**
     * option for Zone Name
     */
    @CommandLine.Option(
            names = {"-zn", "--zoneName"},
            description = "Zone Name",
            arity = BasicStructuresClass.ARITY_ONE_OR_MORE,
            required = true)
    private String[] strZoneName;

    /**
     * option for Zone Name
     */
    @CommandLine.Option(
            names = {"-ld", "--locationDetail"},
            description = "Location details: name,country,division,town",
            arity = BasicStructuresClass.ARITY_ONE_OR_MORE,
            required = true)
    private String[] strLocationDetail;

    @Override
    public void run() {
        int intCounter = 0;
        for (final String crtLocationDetail : strLocationDetail) {
            SunClass.setZoneId(strZoneName[intCounter]);
            SunClass.setLatitude(dblLatitude[intCounter]);
            SunClass.setLongitude(dblLongitude[intCounter]);
            final Properties crtProperties = SunClass.getSunRiseAndSet(crtLocationDetail);
            final String strFeedback = String.format("Details are: %s", crtProperties);
            LogExposureClass.LOGGER.debug(strFeedback);
            intCounter++;
        }
    }

    /**
     * Constructor
     */
    protected CalculateSunriseAndSunset() {
        super();
    }

}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "CaptureChecksumsOfFilesFromFolderIntoCsvFile",
                     description = "Get statistics for all files within a given folder")
class CaptureChecksumsOfFilesFromFoldersIntoCsvFile implements Runnable {

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass optFolderNames = new CommonInteractiveClass.FolderNameOptionMixinClass();
    /**
     * adds the options defined in 
     * CommonInteractiveClass.OutFileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.OutFileNameOptionMixinClass optOutFileName = new CommonInteractiveClass.OutFileNameOptionMixinClass();

    @Override
    public void run() {
        final String[] inAlgorithms = {"SHA-256", "SHA3-256"};
        FileOperationsClass.StatisticsSubClass.setChecksumAlgorithms(inAlgorithms);
        final String[] inFolders = optFolderNames.getFolderNames();
        final String outCsvFile = optOutFileName.getOutFileName();
        for (final String strFolder : inFolders) {
            final ZonedDateTime startComputeTime = ZonedDateTime.now(ZoneId.systemDefault());
            FileOperationsClass.StatisticsSubClass.captureFileStatisticsFromFolder(strFolder, outCsvFile);
            final Duration objDuration = Duration.between(startComputeTime, ZonedDateTime.now(ZoneId.systemDefault()));
            final String strFeedback = String.format("For the folder %s calculated checksums are stored in the file %s operation completed in %s (which means %s | %s)", strFolder, outCsvFile, objDuration.toString(), TimingClass.ConversionSubClass.convertNanosecondsIntoSomething(objDuration, "HumanReadableTime"), TimingClass.ConversionSubClass.convertNanosecondsIntoSomething(objDuration, "TimeClock"));
            LogExposureClass.LOGGER.info(strFeedback);
        }
    }

    /**
     * Constructor
     */
    protected CaptureChecksumsOfFilesFromFoldersIntoCsvFile() {
        super();
    }
}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "CaptureImportsFromJavaSourceFilesIntoCsvFile",
                     description = "Get import inventory from all Java source files within a given folder")
class CaptureImportsFromJavaSourceFilesIntoCsvFile implements Runnable {

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass optFolderNames = new CommonInteractiveClass.FolderNameOptionMixinClass();
    /**
     * adds the options defined in 
     * CommonInteractiveClass.OutFileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.OutFileNameOptionMixinClass optionOut = new CommonInteractiveClass.OutFileNameOptionMixinClass();

    @Override
    public void run() {
        final String[] inFolders = optFolderNames.getFolderNames();
        final String outCsvFile = optionOut.getOutFileName();
        for (final String strFolder : inFolders) {
            FileOperationsClass.ContentReadingSubClass.extractImportStatementsFromJavaSourceFilesIntoCsvFile(Path.of(strFolder), Path.of(outCsvFile));
        }
    }

    /**
     * Constructor
     */
    protected CaptureImportsFromJavaSourceFilesIntoCsvFile() {
        super();
    }
}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "CaptureWindowsApplicationsInstalledIntoCsvFile",
                     description = "Run the experimental new feature")
class CaptureWindowsApplicationsInstalledIntoCsvFile implements Runnable {
    /**
     * adds the options defined in 
     * CommonInteractiveClass.OutFileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.OutFileNameOptionMixinClass optionOut = new CommonInteractiveClass.OutFileNameOptionMixinClass();

    @Override
    public void run() {
        final String outCsvFile = optionOut.getOutFileName();
        ShellingClass.PowerShellExecutionSubClass.captureWindowsApplicationsIntoCsvFile(outCsvFile);
    }

    /**
     * Constructor
     */
    protected CaptureWindowsApplicationsInstalledIntoCsvFile() {
        super();
    }
}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "CleanOlderFilesFromFolder",
                     description = "Clean files older than a given number of days")
class CleanOlderFilesFromFolder implements Runnable {

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass optFolderNames = new CommonInteractiveClass.FolderNameOptionMixinClass();
    /**
     * String for FileName
     */
    @CommandLine.Option(
            names = {"-dLmt", "--daysOlderLimit"},
            description = "Limit number of days to remove files from",
            arity = "1",
            required = true)
    private int intDaysOlderLimit;

    @Override
    public void run() {
        FileOperationsClass.DeletingSubClass.OlderClass.setCleanedFolderStatistics(true);
        final String[] inFolders = optFolderNames.getFolderNames();
        for (final String strFolder : inFolders) {
            FileOperationsClass.DeletingSubClass.OlderClass.setOrResetCleanedFolderStatistics();
            FileOperationsClass.DeletingSubClass.OlderClass.deleteFilesOlderThanGivenDays(strFolder, intDaysOlderLimit);
            final Map<String, Long> statsClndFldr = FileOperationsClass.DeletingSubClass.OlderClass.getCleanedFolderStatistics();
            final String strFeedback = String.format("Folder %s has been cleaned eliminating %s files and freeing %s bytes in terms of disk space...", strFolder, statsClndFldr.get("Files"), statsClndFldr.get("Size"));
            LogExposureClass.LOGGER.info(strFeedback);
        }
    }

    /**
     * Constructor
     */
    protected CleanOlderFilesFromFolder() {
        super();
    }
}

/**
 * Captures sub-folder from a Given Folder into Log file
 */
@CommandLine.Command(name = "GetSubFoldersFromFolders",
                     description = "Captures sub-folders from a Given Folder into Log file")
class GetSubFoldersFromFolders implements Runnable {

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass optFolderNames = new CommonInteractiveClass.FolderNameOptionMixinClass();

    @Override
    public void run() {
        final String[] inFolders = optFolderNames.getFolderNames();
        for (final String strFolder : inFolders) {
            final List<String> arraySubFolders = FileOperationsClass.RetrievingSubClass.getSubFoldersFromFolder(strFolder);
            final String strFeedback = String.format("Considering folder %s following sub-folders were found: %s", strFolder, arraySubFolders);
            LogExposureClass.LOGGER.info(strFeedback);
        }
    }

    /**
     * Private constructor to prevent instantiation
     */
    protected GetSubFoldersFromFolders() {
        super();
    }

}
