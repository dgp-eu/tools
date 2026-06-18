/**
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Shell execution methods
 */
public final class ShellingClass {
    /**
     * Process Capture Need
     */
    private static boolean needProcCapture;
    /**
     * Time-stamp started
     */
    private static LocalDateTime startTimestamp;
    /**
     * Process standard output
     */
    private static String strProcOut;

    /**
     * Building Process for shell execution
     *
     * @param strCommand command to execute
     * @param strParameters command parameters
     */
    private static ProcessBuilder buildProcessForExecution(final String strCommand, final String strParameters) {
        final ProcessBuilder builder = new ProcessBuilder();
        if (strParameters.isEmpty()) {
            builder.command(strCommand);
        } else {
            builder.command(strCommand, strParameters);
        }
        LogExposureClass.exposeProcessBuilder(builder.command().toString());
        builder.directory(FileOperationsClass.RetrievingSubClass.getCurrentUserFolder());
        return builder;
    }

    /**
     * Executes a shells command with/without output captured
     * @param builder ProcessBuilder
     * @param strOutLineSep line separator for the output
     */
    public static void executeShell(final ProcessBuilder builder, final String strOutLineSep) {
        startTimestamp = LocalDateTime.now(ZoneId.systemDefault());
        LogExposureClass.exposeProcessBuilder(builder.command().toString());
        try (Process process = builder.start()) {
            // Read stdout and stderr asynchronously with CompletableFuture
            final CompletableFuture<String> stdoutFuture = CompletableFuture.supplyAsync(() ->
                getStandardReaderIntoString(process.inputReader(), strOutLineSep) // inputReader() = stdout
            );
            final CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(() ->
                getStandardReaderIntoString(process.errorReader(), strOutLineSep) // errorReader() = stderr
            );
            final int exitCode = process.waitFor();
            CompletableFuture.allOf(stdoutFuture, stderrFuture).join();
            setProcessResults(stdoutFuture, exitCode);
            process.destroy();
        } catch (IOException ex) {
            final String strFeedback = String.format("Process execution failed: %s", Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        } catch(InterruptedException ei) {
            final String strFeedback = String.format("Execution was interrupted... %s", Arrays.toString(ei.getStackTrace()));
            LogExposureClass.LOGGER.warn(strFeedback);
            /* Clean up whatever needs to be handled before interrupting  */
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Executes a shells command with capturing the output to a String
     *
     * @param strCommand command to execute
     * @param strParameters command parameters
     * @param strOutLineSep line separator for the output
     */
    public static void executeShellUtility(final String strCommand, final String strParameters, final String strOutLineSep) {
        final ProcessBuilder builder = buildProcessForExecution(strCommand, strParameters);
        setProcessCaptureNeed(true);
        executeShell(builder, strOutLineSep);
    }

    /**
     * Getting current logged account name
     * @return String
     */
    public static String getCurrentUserAccount() {
        setProcessCaptureNeed(true);
        executeShellUtility("WHOAMI", "/UPN", "");
        String strUser = strProcOut;
        if ((strUser == null)
                || strUser.startsWith("ERROR:")) {
            final String strFeedback = "ERROR: Unable to get User Principal Name (UPN), hence will get regular User Name";
            LogExposureClass.LOGGER.error(strFeedback);
            executeShellUtility("WHOAMI", "", "");
            strUser = strProcOut;
        }
        if (strUser == null) {
            strUser = "";
        }
        return strUser;
    }

    /**
     * Setter for Process output and error
     * @param stdoutFuture Process output
     * @param exitCode process execution exit code
     */
    private static void setProcessResults(
            final CompletableFuture<String> stdoutFuture,
            final int exitCode) {
        final String strCaptureMessage;
        if (needProcCapture) {
            try {
                strProcOut = stdoutFuture.get();
            } catch (InterruptedException ei) {
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
                final String strFeedback = String.format("Execution was interrupted... %s", Arrays.toString(ei.getStackTrace()));
                LogExposureClass.LOGGER.warn(strFeedback);
            } catch (ExecutionException ee) {
                final String strFeedback = String.format("Execution exception tracing %s", Arrays.toString(ee.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
                throw (IllegalStateException)new IllegalStateException().initCause(ee);
            }
            strCaptureMessage = "Process execution WITH output captured completed with exit code %d";
        } else {
            strCaptureMessage = "Process execution w/o output captured completed with exit code %d";
        }
        final LocalDateTime finishTimeStamp = LocalDateTime.now(ZoneId.systemDefault());
        final String strFeedback = TimingClass.logDuration(startTimestamp,
                finishTimeStamp,
                String.format(strCaptureMessage, exitCode));
        LogExposureClass.LOGGER.debug(strFeedback);
    }

    /**
     * collect Standard Reader into String
     * @param reader BufferedReader content
     * @param strOutLineSep line separators
     * @return String
     */
    private static String getStandardReaderIntoString(final BufferedReader reader, final String strOutLineSep) {
        return reader.lines().collect(Collectors.joining(strOutLineSep)).trim();
    }

    /**
     * Setter for Process Capture
     * @param inProcCapture boolean
     */
    public static void setProcessCaptureNeed(final boolean inProcCapture) {
        needProcCapture = inProcCapture;
    }

    /**
     * PowerShell execution
     */
    public static final class PowerShellExecutionSubClass {
        /**
         * PowerShell file
         */
        private static String psPath;

        private static String[] buildWindowsApplicationCommandSafely(final String strFileName) {
            final String strCmd = String.format(
                "Get-ItemProperty HKLM:\\Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\* | " +
                "Select-Object Publisher, DisplayName, DisplayVersion, EngineVersion, InstallDate, EstimatedSize, URLInfoAbout | " +
                "Export-Csv -Encoding utf8 -Path '%s' -UseCulture -NoTypeInformation -Force",
                strFileName
            );
            final String[] arrayCommand = { psPath, "-Command", strCmd };
            final String strFeedback = String.format("PowerShell command to be executed is: %s", Arrays.toString(arrayCommand));
            LogExposureClass.LOGGER.debug(strFeedback);
            return arrayCommand;
        }

        /**
         * Capture Windows installed application into a CSV file
         */
        public static void captureWindowsApplicationsIntoCsvFile(final String strFileName) {
            final String crtOperatingSys = System.getProperty("os.name");
            if (crtOperatingSys.startsWith("Windows")) {
                try {
                    final String[] varsToPick = {"osWindowsSystem32Path", "powerShellBinary"};
                    final Properties svProperties = BasicStructuresClass.PropertiesReaderSubClass.getVariableFromProjectProperties("/project.properties", varsToPick);
                    setPowerShellFile(svProperties.get("powerShellBinary").toString());
                    validatePathEnvironmentVariable();
                    final String[] arrayCommand = buildWindowsApplicationCommandSafely(strFileName);
                    final ProcessBuilder builder = new ProcessBuilder(arrayCommand);
                    builder.directory(new File(svProperties.get("osWindowsSystem32Path").toString()));
                    setProcessCaptureNeed(false);
                    executeShell(builder, System.lineSeparator());
                } catch (SecurityException se) {
                    final String strFeedback = String.format("Security violation:  %s", Arrays.toString(se.getStackTrace()));
                    LogExposureClass.LOGGER.error(strFeedback);
                }
            }
        }

        /**
         * Validate that the executable exists and is not writable by non-admin users
         * @param psBinary PowerShell binary
         */
        private static void setPowerShellFile(final String psBinary) {
            final File psFileLocal = new File(psBinary);
            if (psFileLocal.exists() && psFileLocal.canExecute()) {
                psPath = psBinary;
            } else {
                final String strFeedback = String.format("Security violation: PowerShell executable not found or not executable: %s...", psBinary);
                LogExposureClass.LOGGER.error(strFeedback);
                throw new SecurityException(strFeedback);
            }
        }

        /**
         * Validate PATH environment variable (optional hardening)
         */
        private static void validatePathEnvironmentVariable() {
            final String pathEnv = System.getenv().get("PATH");
            if (pathEnv != null) {
                final String[] arraysPathFolders = pathEnv.split(";");
                final List<String> arrayPaths = Arrays.asList(arraysPathFolders);
                arrayPaths.forEach(crtPath -> {
                    final File pathDir = new File(crtPath);
                    if (pathDir.exists() && pathDir.canWrite()) {
                        final String strFeedback = String.format("Security violation: Writable directory detected in PATH: %s...", crtPath);
                        LogExposureClass.LOGGER.error(strFeedback);
                        throw new SecurityException(strFeedback);
                    }
                });
            }
        }

        /**
         * Constructor
         */
        private PowerShellExecutionSubClass() {
            // intentionally left blank
        }

    }

    /**
     * Constructor
     */
    private ShellingClass() {
        // intentionally left blank
    }

}
