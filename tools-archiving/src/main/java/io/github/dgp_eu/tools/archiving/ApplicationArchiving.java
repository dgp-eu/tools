/*
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.archiving;

import io.github.dgp_eu.tools.cli.CommonInteractiveClass;
import io.github.dgp_eu.tools.core.*;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

import java.util.Properties;

/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
            ArchiveFolders.class
    }
)
public final class ApplicationArchiving {

    /**
     * Constructor empty
     */
    private ApplicationArchiving() {
        super();
    }

    /**
     * default
     * @param args input arguments
     */
    static void main( final String[] args ) {
        CommonInteractiveClass.startMeUpWithParameters("logs/DGP-EU_Tools-Archiving-", "/tools-archiving-pom.xml");
        final int intArchExitCode = new CommandLine(new ApplicationArchiving()).execute(args);
        CommonInteractiveClass.shutMeDownWithParameters(intArchExitCode, args[0]);
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
