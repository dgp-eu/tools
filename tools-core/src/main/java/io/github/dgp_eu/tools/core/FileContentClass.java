package io.github.dgp_eu.tools.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.dgp_eu.tools.core.time.TimingClass;

/**
 * File content handling methods
 */
public final class FileContentClass {
    /** Column Separator for CSV file writing methods */
    /* default */ private static char chCsvColSeparator = ';';

    /**
     * Constructor
     */
    private FileContentClass() {
        super();
    }

    /**
     * Setter for Column Separator for CSV file writing methods
     * @param inCsvColSeparator char
     */
    public static void setCsvColumnSeparator(final char inCsvColSeparator) {
        chCsvColSeparator = inCsvColSeparator;
    }

    /**
     * File Content Reading
     */
    public static final class ContentReadingSubClass {

        /**
         * Capture Import Statements from Java source files into CSV
         * @param inJavaSources folder with Java source files
         * @param outCsvFile CSV file to write results into
         */
        public static void extractImportStatementsFromJavaSourceFilesIntoCsvFile(final Path inJavaSources, final Path outCsvFile) {
            final String strImport = "import ";
            final String strCleanRegEx = String.format("(%s|%s)", strImport, chCsvColSeparator);
            final String dtNow = TimingClass.getCurrentDateTimeLocalRaw();
            try (BufferedWriter writer = Files.newBufferedWriter(outCsvFile, StandardCharsets.UTF_8)) {
                writer.write("Path;File;Imported;Timestamp");
                writer.newLine();
                final List<Path> arrayFiles = FileOperationsClass.RetrievingSubClass.getSpecificFilesFromFolderRecursive(inJavaSources, "java");
                arrayFiles.forEach(crtFileName -> {
                    try (BufferedReader reader = Files.newBufferedReader(crtFileName, StandardCharsets.UTF_8)) {
                        String line = reader.readLine();  // Initialize the variable outside the loop
                        long lineCounter = 0;
                        while (Objects.nonNull(line)
                                && (lineCounter < 100)) {
                            if (line.startsWith(strImport)) {
                                writer.write(crtFileName.getParent().toString()
                                        + chCsvColSeparator + crtFileName.getFileName().toString()
                                        + chCsvColSeparator + line.replaceAll(strCleanRegEx, "")
                                        + chCsvColSeparator + dtNow);
                                writer.newLine();
                            }
                            line = reader.readLine();  // Update the variable within the loop, not in the condition
                            lineCounter++;
                        }
                        final String strFeedback = String.format("File %s has been digested...", crtFileName);
                        LogExposureClass.LOGGER.debug(strFeedback);
                    } catch (IOException ei) {
                        LogExposureClass.exposeInputOutputException(Arrays.toString(ei.getStackTrace()));
                    }
                });
            } catch (IOException ei) {
                LogExposureClass.exposeInputOutputException(Arrays.toString(ei.getStackTrace()));
            }
        }

        /**
         * Get file content into String
         * (either included in JAR or from Disk/Storage)
         * @param strFileName file name in scope
         * @return file content
         */
        public static String getFileContentIntoString(final String strFileName) {
            final String strOutput;
            if (BasicStructuresClass.isRunningFromJar()) {
                strOutput = getJarIncludedFileContentIntoString(strFileName);
            } else {
                strOutput = getDiskFileContentIntoString(strFileName);
            }
            return strOutput;
        }

        /**
         * Get file content into String
         * (good for small files, bad for JAR included files)
         * @param strFileName file name
         * @return String
         */
        private static String getDiskFileContentIntoString(final String strFileName) {
            final String strFeedback = String.format("Attempting to get content into a String from %s file...", strFileName);
            LogExposureClass.LOGGER.debug(strFeedback);
            String strReturn = "";
            try {
                strReturn = Files.readString(Path.of(strFileName));
            } catch (IOException e) {
                final String strFeedbackErr = String.format("Error when attempting to get content of file \"%s\": %s", strFileName, Arrays.toString(e.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
            return strReturn;
        }

        /**
         * Get file content into InputStream
         * @param strFileName file name
         * @return input stream
         */
        private static String getJarIncludedFileContentIntoString(final String strFileName) {
            String strContent = null;
            final String strFeedback = String.format("Attempting to get content into a String from %s file...", strFileName);
            LogExposureClass.LOGGER.debug(strFeedback);
            try (InputStream iStream = ContentReadingSubClass.class.getResourceAsStream(strFileName)) {
                assert iStream != null;
                try (InputStreamReader inputStreamReader = new InputStreamReader(iStream, StandardCharsets.UTF_8);
                     BufferedReader bReader = new BufferedReader(inputStreamReader)) {
                    strContent = bReader.readAllAsString();
                    final String strFeedbackOk = String.format("I have successfully loaded entire content from %s file into stream...", strFileName);
                    LogExposureClass.LOGGER.debug(strFeedbackOk);
                }
            } catch (IOException ex) {
                final String strFeedbackErr = String.format("Error \"%s\"", Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
            return strContent;
        }

        /**
         * Getting list of values from a column grouped by another column
         * @param strFileName target file name to be written to
         * @param intColToEval number of column to evaluate (build values list from it)
         * @param intColToGrpBy number of column to group list of values by
         * @return Map with String and List or String
         */
        public static Map<String, List<String>> getListOfValuesFromColumnGroupedByAnotherColumnValuesFromCsvFile(
                final String strFileName,
                final Integer intColToEval,
                final Integer intColToGrpBy) {
            Map<String, List<String>> grouped = new ConcurrentHashMap<>();
            try (Stream<String> lines = Files.lines(Path.of(strFileName))) {
                // Group values by category
                grouped = lines
                        .skip(1)
                        .map(line -> line.split("\",\"")) // split by comma
                        .collect(Collectors.groupingBy(
                                cols -> cols[intColToGrpBy], // key = Category
                                Collectors.mapping(cols -> cols[intColToEval].replace("\"", ""), Collectors.toList()) // values
                        ));
            } catch (IOException ex) {
                final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
            return grouped;
        }

        /**
         * Constructor
         */
        private ContentReadingSubClass() {
            // intentionally blank
        }

    }

    /**
     * File Content Reading
     */
    public static final class ContentWritingSubClass {
        /** Line Prefix for CSV content writing methods */
        private static String strCsvLinePrefix = "";

        /**
         * Setter for Line prefix for CSV file writing methods
         * @param inCsvLinePrefix String
         */
        public static void setCsvLinePrefix(final String inCsvLinePrefix) {
            strCsvLinePrefix = inCsvLinePrefix + chCsvColSeparator;
        }

        /**
         * storing into a CSV file a LinkedHashMap
         * @param strFileName target file name to be written to
         * @param strHeader header values
         * @param listHsMp LinkedHashMap
         */
        public static void writeLinkedHashMapToCsvFile(final String strFileName, final String strHeader, final Map<String, Long> listHsMp) {
            try {
                final List<String> strLines;
                final File strFile = new File(strFileName);
                if (strFile.exists()) {
                    strLines = listHsMp.entrySet().stream()
                            .map(e -> strCsvLinePrefix + e.getKey() + chCsvColSeparator + e.getValue())
                            .toList();
                } else {
                    strLines = Stream.concat(
                            Stream.of(strHeader), // header
                            listHsMp.entrySet().stream()
                                    .map(e -> strCsvLinePrefix + e.getKey() + chCsvColSeparator + e.getValue())
                    ).toList();
                }
                Files.write(Path.of(strFileName), strLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException ex) {
                final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }

        /**
         * Write list of single values to File
         * 
         * @param listStrings List of Strings
         * @param strFileName file name to write to
         */
        public static void writeListToTextFile(final String strFileName, final List<String> listStrings) {
            FileOperationsClass.DeletingSubClass.deleteFileIfExists(strFileName);
            try (BufferedWriter bwr = Files.newBufferedWriter(Path.of(strFileName), StandardCharsets.UTF_8)) {
                listStrings.forEach(strLine -> {
                    try {
                        bwr.write(strLine);
                        bwr.newLine();
                    } catch (IOException er) {
                        final String strFeedback = String.format("Error encountered when attempting to write to %s file... %s", strFileName, Arrays.toString(er.getStackTrace()));
                        LogExposureClass.LOGGER.error(strFeedback);
                    }
                });
                final String strFeedback = String.format("Writing list to %s file completed successfully!", strFileName);
                LogExposureClass.LOGGER.debug(strFeedback);
            } catch (IOException ex) {
                final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }

        /**
         * Write list of Properties to CSV File
         *
         * @param strFileName target File
         * @param propertiesList list of Properties
         */
        public static void writePropertiesListToCsvFile(final String strFileName, final List<Properties> propertiesList) {
            // Collect all unique keys
            final Set<String> allKeys = new LinkedHashSet<>();
            for (final Properties properties : propertiesList) {
                allKeys.addAll(properties.stringPropertyNames());
            }
            final String strClmnSeparator = String.valueOf(chCsvColSeparator);
            try (BufferedWriter bwr = Files.newBufferedWriter(Path.of(strFileName), StandardCharsets.UTF_8)) {
                // Write the header
                bwr.write(String.join(strClmnSeparator, allKeys));
                bwr.newLine();
                final Set<String> row = new LinkedHashSet<>();
                // Write each row
                for (final Properties properties : propertiesList) {
                    row.clear();
                    for (final String key : allKeys) {
                        row.add(properties.getProperty(key, "")); // Supply default value "" if key is absent
                    }
                    bwr.write(String.join(strClmnSeparator, row));
                    bwr.newLine();
                }
            } catch (IOException ex) {
                final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }

        /**
         * Store small content into file
         * @param strFileName destination file name
         * @param strRawText content
         */
        public static void writeRawTextToFile(final String strFileName, final String strRawText) {
            FileOperationsClass.DeletingSubClass.deleteFileIfExists(strFileName);
            try (BufferedWriter bwr = Files.newBufferedWriter(Path.of(strFileName), StandardCharsets.UTF_8)) {
                bwr.write(strRawText);
                final String strFeedback = String.format("Writing list to %s file completed successfully!", strFileName);
                LogExposureClass.LOGGER.debug(strFeedback);
            } catch (IOException ex) {
                final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }

        /**
         * storing into a CSV file a LinkedHashMap
         * @param strFileName target file name to be written to
         * @param strHeader header values
         * @param listStrings List of String
         */
        public static void writeStringListToCsvFile(final String strFileName, final String strHeader, final List<String> listStrings) {
            try {
                final List<String> strLines;
                final File strFile = new File(strFileName);
                if (strFile.exists()) {
                    strLines = listStrings.stream()
                            .map(value -> strCsvLinePrefix + value)
                            .toList();
                } else {
                    strLines = Stream.concat(
                            Stream.of(strHeader), // header
                            listStrings.stream()
                                    .map(value -> strCsvLinePrefix + value)
                    ).toList();
                }
                Files.write(Path.of(strFileName), strLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException ex) {
                final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }

        /**
         * Constructor
         */
        private ContentWritingSubClass() {
            // intentionally blank
        }

    }

}
