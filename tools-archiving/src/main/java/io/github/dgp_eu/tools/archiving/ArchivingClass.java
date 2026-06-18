/*
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.archiving;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.core.ShellingClass;

/**
 * Archiving wrapper
 */
public final class ArchivingClass {
    /**
     * Archive Name variable
     */
    private static String strArchiveName;
    /**
     * Archive Prefix variable
     */
    private static String strArchivePrefix;
    /**
     * Archive Password variable
     */
    private static String strArchivePwd;
    /**
     * Archive Suffix variable
     */
    private static String strArchiveSuffix;
    /**
     * Archive Folder variable
     */
    private static String strArchivingDir;
    /**
     * Archive Executable variable
     */
    private static String strArchivingExec;

    /**
     * Append File Separator to given Folder 
     * @param inFolder folder given
     * @return String
     */
    private static String appendSeparatorSuffixToFolder(final String inFolder) {
        final StringBuilder sbFolder = new StringBuilder();
        final int intLength = inFolder.length();
        if (inFolder.substring(intLength - 1, intLength).equalsIgnoreCase(File.separator)) {
            sbFolder.append(inFolder.substring(0, intLength - 1));
        } else {
            sbFolder.append(inFolder);
        }
        sbFolder.append(File.separator);
        return sbFolder.toString();
    }

    /**
     * Archive folder content as 7z using external binary
     */
    public static void archiveFolderAs7z() {
        final String strArchDir = "-ir!" + strArchivingDir.replace("\"", "") + "*";
        final ProcessBuilder builder;
        if (strArchivePwd == null) {
            builder = new ProcessBuilder(strArchivingExec, "a", "-t7z", strArchiveName, strArchDir, "-mx9", "-ms4g", "-mmt=on");
        } else {
            builder = new ProcessBuilder(strArchivingExec, "a", "-t7z", strArchiveName, strArchDir, "-mx9", "-ms4g", "-mmt=on", "-p" + strArchivePwd);
            LogExposureClass.exposeProcessBuilder(builder.command().toString().replaceFirst("-p" + strArchivePwd, "**H*I*D*D*E*N**P*A*S*S*W*O*R*D**"));
        }
        ShellingClass.setProcessCaptureNeed(false);
        ShellingClass.executeShell(builder, " ");
    }

    /**
     * Log Archived content
     * @param folderProps folder Properties
     */
    public static void exposeArchivedStatistics(final Properties folderProps) {
        if (strArchiveName != null) {
            final String strFeedback = String.format("Analyzing archive file %s...", strArchiveName);
            LogExposureClass.LOGGER.info(strFeedback);
            final File fileA = new File(strArchiveName.replace("\"", ""));
            if (fileA.exists()
                    && fileA.isFile()) {
                final String strFeedback2 = String.format("Archive file %s exists and is identified as a file...",
                        strArchiveName);
                LogExposureClass.LOGGER.info(strFeedback2);
                final long fileArchSize = fileA.length();
                final Object sizeBytesObj = folderProps.getOrDefault("SIZE_BYTES", "0");
                final String sizeBytesStr = sizeBytesObj == null ? "0" : sizeBytesObj.toString();
                long fileOrigSize;
                try {
                    fileOrigSize = Long.parseLong(sizeBytesStr);
                } catch (NumberFormatException _) {
                    LogExposureClass.LOGGER.warn("Invalid SIZE_BYTES value '{}', defaulting to 0.", sizeBytesStr);
                    fileOrigSize = 0L;
                }
                final float percentage = BasicStructuresClass.computePercentageSafely(fileArchSize, fileOrigSize);
                final String strFeedbackFinal = String.format("Folder %s statistics are %s which was compressed to " +
                        "archive %s having a size of %s bytes (which is %s%% of the original)",
                        strArchivingDir.replace("\"", ""), folderProps, strArchiveName, fileArchSize, percentage);
                LogExposureClass.LOGGER.info(strFeedbackFinal);
            }
        }
    }

    /**
     * Setter for Archive Name
     * @param inArchiveName String
     */
    public static void setArchiveName(final String inArchiveName) {
        final StringBuilder sbArchiveName = new StringBuilder();
        if (strArchivePrefix != null) {
            sbArchiveName.append(strArchivePrefix);
        }
        sbArchiveName.append(inArchiveName);
        if (strArchiveSuffix != null) {
            sbArchiveName.append(strArchiveSuffix);
        }
        sbArchiveName.append(".7z");
        strArchiveName = BasicStructuresClass.StringTransformationSubClass.encloseStringIfContainsSpace(sbArchiveName.toString(), '\"');
    }

    /**
     * Setter for Archive Name from Folder Name
     * @param inFolderDest destination folder
     */
    public static void setArchiveNameWithinDestinationFolder(final String inFolderDest) {
        final Path path = Paths.get(strArchivingDir.replace("\"", ""));
        setArchiveName(appendSeparatorSuffixToFolder(inFolderDest.replace("\"", ""))
                + path.getFileName().toString());
    }

    /**
     * Setter for Archive Name
     * @param inArchivePrefix String
     */
    public static void setArchivePrefix(final String inArchivePrefix) {
        strArchivePrefix = inArchivePrefix;
    }

    /**
     * Setter for Archive Name
     * @param inArchivePwd String
     */
    public static void setArchivePwd(final String inArchivePwd) {
        String strGivenPassword = inArchivePwd;
        if (inArchivePwd.matches("[A-Z0-9_]+")) {
            strGivenPassword = System.getenv(inArchivePwd); // get password value from Environment variable
        }
        strArchivePwd = BasicStructuresClass.StringTransformationSubClass.encloseStringIfContainsSpace(strGivenPassword, '\"');
    }

    /**
     * Setter for Archive Suffix
     * @param inArchiveSuffix String
     */
    public static void setArchiveSuffix(final String inArchiveSuffix) {
        strArchiveSuffix = inArchiveSuffix;
    }

    /**
     * Setter for Archive Folder
     * @param inArchivingDir String
     */
    public static void setArchivingDir(final String inArchivingDir) {
        strArchivingDir = appendSeparatorSuffixToFolder(inArchivingDir);
    }

    /**
     * Setter for Archive Executable
     * @param inArchivingExec String
     */
    public static void setArchivingExecutable(final String inArchivingExec) {
        strArchivingExec = BasicStructuresClass.StringTransformationSubClass.encloseStringIfContainsSpace(inArchivingExec, '\"');
    }

    /**
     * Constructor
     */
    private ArchivingClass() {
        // intentionally blank
    }

}
