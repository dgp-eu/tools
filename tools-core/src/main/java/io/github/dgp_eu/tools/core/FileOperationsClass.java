/** Copyright 2026 Daniel-Gheorghe Popiniuc */
package io.github.dgp_eu.tools.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import io.github.dgp_eu.tools.core.time.TimingClass;

/**
 * File Operations
 */
public final class FileOperationsClass {
    /** Localized String for File Finding error */
    public static final String FILE_FIND_ERR = "Error encountered when attempting to get %s file(s) from %s folder";

    /**
     * File Deletion logic
     */
    public static final class DeletingSubClass {

        /**
         * Removes a files if already exists
         *
         * @param strFileName file name to search
         */
        public static void deleteFileIfExists(final String strFileName) {
            try {
                final Path filePath = Path.of(strFileName);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                final String strFeedback = String.format("Error encountered when attempting to write to %s file... %s", strFileName, Arrays.toString(e.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }

        /**
         * Deletes all files matching given pattern from folder
         * @param strFolder input folder
         * @param strPattern input pattern
         */
        public static void deleteFilesMatchingPatternFromFolder(final String strFolder, final String strPattern) {
            try {
                final String strFeedback = String.format("I will attempt to removed all matched files based on %s pattern from folder %s...", strPattern, strFolder);
                LogExposureClass.LOGGER.info(strFeedback);
                final Path dir = Path.of(strFolder);
                Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                    @Override
                    @NonNull
                    public FileVisitResult visitFile(@NonNull final Path file, @NonNull final BasicFileAttributes attrs) throws IOException {
                        if (file.getFileName().toString().matches(strPattern)) {
                            Files.delete(file);
                            final String strFeedbackD = String.format("File %s has been deleted", file);
                            LogExposureClass.LOGGER.info(strFeedbackD);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ei) {
                final String strFeedbackErr = String.format("Inout/Output exception on... %s", Arrays.toString(ei.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
        }

        /**
         * File deleting logic
         */
        public static final class OlderSubSubClass {
            /**
             * Cleaned Folder Statistics
             */
            private static boolean bolClnFldrStats;
            /**
             * Counter for removed files
             */
            private static long lngFilesClnd;
            /**
             * Size in bytes for removed files
             */
            private static long lngByteSizeClnd;

            /**
             * Getter for Cleaned Folder Statistics
             * @return Map with folder statistics
             */
            public static Map<String, Long> getCleanedFolderStatistics() {
                final Map<String, Long> statsClndFldr = new ConcurrentHashMap<>();
                statsClndFldr.put("Files", lngFilesClnd);
                statsClndFldr.put(ConfigurationClass.STR_SIZE, lngByteSizeClnd);
                return statsClndFldr;
            }

            /**
             * Get list of sub-folders from a given folder
             *
             * @param strFolderName folder name to look into
             * @param intOlderLimit older days limit
             */
            public static void deleteFilesOlderThanGivenDays(final String strFolderName, final long intOlderLimit) {
                final long cutoff = TimingClass.getDaysAgoWithMillisecondsPrecision(intOlderLimit);
                final String strFeedback = String.format("Will attempt to remove all files older than \"%s\" from within \"%s\" folder...",
                        TimingClass.getEpochMilliseconds(cutoff), strFolderName);
                LogExposureClass.LOGGER.debug(strFeedback);
                final Path directory = Path.of(strFolderName);
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
                    for (final Path entry : stream) {
                        if (Files.isDirectory(entry)) {
                            deleteFilesOlderThanGivenDays(entry.toString(), intOlderLimit);
                        } else if (Files.isRegularFile(entry)) {
                            deleteFilesOlderThanGivenDaysWithoutChecks(entry, cutoff);
                        }
                    }
                } catch (IOException ex) {
                    final String strFeedbackErr = String.format("Error encountered when attempting to get sub-folders from %s folder... %s", strFolderName, Arrays.toString(ex.getStackTrace()));
                    LogExposureClass.LOGGER.error(strFeedbackErr);
                }
            }

            /**
             * Remove files older than given days without checks
             * @param entry Path to file
             * @param cutoff cutoff time in milliseconds
             * @throws IOException check exception
             */
            private static void deleteFilesOlderThanGivenDaysWithoutChecks(final Path entry, final long cutoff) throws IOException {
                final BasicFileAttributes attr = Files.readAttributes(entry, BasicFileAttributes.class);
                final long modifTime = attr.lastModifiedTime().toMillis();
                if (modifTime <= cutoff) {
                    Files.delete(entry);
                    if (bolClnFldrStats) {
                        lngFilesClnd = lngFilesClnd + 1;
                        lngByteSizeClnd = lngByteSizeClnd + attr.size();
                    }
                }
            }

            /**
             * Setter for Cleaned Folder Statistics
             * @param inStats boolean
             */
            public static void setCleanedFolderStatistics(final boolean inStats) {
                if (bolClnFldrStats != inStats) {
                    setOrResetCleanedFolderStatistics();
                }
                bolClnFldrStats = inStats;
            }

            /**
             * Setter/Resetter for Cleaned Folder Statistics
             */
            public static void setOrResetCleanedFolderStatistics() {
                lngFilesClnd = 0;
                lngByteSizeClnd = 0;
            }

            /**
             * Constructor
             */
            private OlderSubSubClass() {
                // intentionally blank
            }

        }

        /**
         * Constructor
         */
        private DeletingSubClass() {
            // intentionally blank
        }

    }

    /**
     * File Mass Change logic
     */
    public static final class MassFileChangeSubClass {
        /** holding characters being replaced */
        private static String existingContent;
        /** holding characters to replace it with */
        private static String replacedContent;
        /** variable for folder */
        private static String strFolder;
        /** variable for pattern */
        private static String strPattern;

        /**
         * Build new file from existing one
         * @param existingFile existing file as Path
         * @return Path
         */
        private static Path getNewFile(final Path existingFile) {
            final String newPath = existingFile.getParent().toString();
            final String newFileName = existingFile.getFileName().toString().replace(".json", "-new.json");
            return Path.of(newPath).resolve(newFileName);
        }

        /**
         * Change String to all files within a folder based on a pattern
         */
        public static void massChangeToFilesWithinFolder() {
            try {
                final String strFeedback = String.format("I will attempt to mass change all matched files based on %s pattern from folder %s...", strPattern, strFolder);
                LogExposureClass.LOGGER.info(strFeedback);
                final Path dir = Path.of(strFolder);
                Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                    @Override
                    @NonNull
                    public FileVisitResult visitFile(@NonNull final Path file, @NonNull final BasicFileAttributes attrs) {
                        if (file.getFileName().toString().matches(strPattern)) {
                            secureModify(file);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ei) {
                final String strFeedbackErr = String.format("Inout/Output exception on... %s", Arrays.toString(ei.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
        }

        /**
         * secure modification
         * @param file file to write to
         */
        private static void secureModify(final Path file) {
            final Path newFile = getNewFile(file);
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
                 BufferedWriter writer = Files.newBufferedWriter(newFile, StandardCharsets.UTF_8)) {
                String line = reader.readLine();  // Initialize the variable outside the loop
                while (Objects.nonNull(line)) {
                    writer.write(line.replace(existingContent, replacedContent));
                    writer.newLine(); // Reintroduce the line separator
                    line = reader.readLine();  // Update the variable within the loop, not in the condition
                }
                // Replace the original file with the modified content
                Files.move(newFile, file, StandardCopyOption.REPLACE_EXISTING);
                final String strFeedback = String.format("File %s has been modified...", file);
                LogExposureClass.LOGGER.debug(strFeedback);
            } catch (IOException ei) {
                LogExposureClass.exposeInputOutputException(Arrays.toString(ei.getStackTrace()));
            }
        }

        /**
         * Setter for replacedContent
         * @param inNewContent new content
         */
        public static void setNewContent(final String inNewContent) {
            replacedContent = inNewContent;
        }

        /**
         * Setter for existingContent
         * @param inOldContent old content
         */
        public static void setOldContent(final String inOldContent) {
            existingContent = inOldContent;
        }

        /**
         * Setter for strPattern
         * @param inPattern pattern for file matching
         */
        public static void setPattern(final String inPattern) {
            strPattern = inPattern;
        }

        /**
         * Setter for strDestinationFolder
         * @param inFolder destination folder
         */
        public static void setSearchingFolder(final String inFolder) {
            strFolder = inFolder;
        }

        /**
         * Constructor
         */
        private MassFileChangeSubClass() {
            // intentionally blank
        }
    }

    /**
     * File Moving logic
     */
    public static final class MovingSubClass {

        /**
         * Archives single file to new location
         * 
         * @param strFileName file name in scope for archival
         * @param strDestFolder destination folder
         */
        public static void moveFileToNewLocation(final String strFileName, final String strDestFolder) {
            try {
                final String strFeedbackBefore = String.format("Will attempt to move \"%s\" file to \"%s\" folder", strFileName, strDestFolder);
                LogExposureClass.LOGGER.info(strFeedbackBefore);
                Files.move(Path.of(strFileName), Path.of(strDestFolder), StandardCopyOption.REPLACE_EXISTING);
                final String strFeedbackAfter = String.format("Success file \"%s\" has been moved to \"%s\" folder", strFileName, strDestFolder);
                LogExposureClass.LOGGER.info(strFeedbackAfter);
            } catch (IOException ex) {
                final String strFeedback = String.format("Error when attempting to move \"%s\" file to \"%s\": %s", strFileName, strDestFolder, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }

        /**
         * Constructor
         */
        private MovingSubClass() {
            // intentionally blank
        }

    }

    /**
     * File Content Reading
     */
    public static final class RetrievingSubClass {

        /**
         * Checking if a file exists and is readable
         * @param fileSize file size
         * @param strFileName file name
         * @return Properties
         */
        public static Properties checkFileExistanceAndReadability(final long fileSize, final String strFileName) {
            final Properties propertiesReturn = new Properties();
            switch(String.valueOf(fileSize)) {
                case "-1":
                    propertiesReturn.put("NOT_READABLE", String.format("Given file %s is NOT a readable file...", strFileName));
                    break;
                case "-2":
                    propertiesReturn.put("NOT_A_FILE", String.format("Given file %s is not really a file...", strFileName));
                    break;
                case "-3":
                    propertiesReturn.put("DOES_NOT_EXIST", String.format("Given file %s does NOT exist...", strFileName));
                    break;
                case "-99":
                    propertiesReturn.put("NULL_FILE_NAME", "Given file %s does NOT exist...");
                    break;
                default:
                    propertiesReturn.put("OK", strFileName);
                    break;
            }
            return propertiesReturn;
        }

        /**
         * Checking if a file exists and is readable
         * @param strFileName file name
         * @return Properties
         */
        public static Properties checkFileExistanceAndReadability(final String strFileName) {
            final long fileSize = getFileSizeIfFileExistsAndIsReadable(strFileName);
            return checkFileExistanceAndReadability(fileSize, strFileName);
        }

        /**
         * Getting current user
         * 
         * @return File
         */
        public static File getCurrentUserFolder() {
            return new File(System.getProperty("user.home"));
        }

        /**
         * Gets file size if exits and is readable
         * @param strFileName file name
         * @return long
         */
        public static long getFileSizeIfFileExistsAndIsReadable(final String strFileName) {
            final long fileSize;
            if (strFileName == null
                    || strFileName.isBlank()) {
                fileSize = -99;
            } else {
                final File fileGiven = new File(strFileName);
                if (fileGiven.exists()) {
                    fileSize = getFileSizeAfterEnsuringItExists(fileGiven);
                } else {
                    fileSize = -3;
                }
            }
            return fileSize;
        }

        /**
         * Gets file size if exits and is readable
         * @param fileGiven file name
         * @return long
         */
        private static long getFileSizeAfterEnsuringItExists(final File fileGiven) {
            final long fileSize;
            if (fileGiven.isFile()) {
                if (fileGiven.canRead()) {
                    fileSize = fileGiven.length();
                } else {
                    fileSize = -1;
                }
            } else {
                fileSize = -2;
            }
            return fileSize;
        }

        /**
         * get internal file size from Disk or inside Jar
         * @param strFilePath   input Path
         * @param isExecFromJar is execution from Jar
         * @return size of the file
         */
        public static long getInternalFileSize(final String strFilePath, final boolean isExecFromJar) {
            final String strFilePathDisk = ConfigurationClass.getCurrentFolder()
                    + "/src/main/resources" + strFilePath;
            long fileSizeActual = getFileSizeIfFileExistsAndIsReadable(strFilePathDisk);
            if (isExecFromJar
                    || fileSizeActual < 0) {
                try (InputStream inStream = Objects.requireNonNull(RetrievingSubClass.class.getResourceAsStream(strFilePath),
                        "Resource not found: " + strFilePath)) {
                    // transferTo returns the number of bytes transferred (Java 9+)
                    fileSizeActual = inStream.transferTo(OutputStream.nullOutputStream());
                } catch (IOException ei) {
                    LogExposureClass.exposeInputOutputException(Arrays.toString(ei.getStackTrace()));
                }
            }
            return fileSizeActual;
        }

        /**
         * Get list of files from a given folder that may have sub-folders
         * @param inFolderName folder name to look into
         * @param strExtension extension to isolate
         * @return List of Strings
         */
        public static List<Path> getSpecificFilesFromFolderRecursive(final Path inFolderName, final String strExtension) {
            List<Path> arrayFiles = List.of();
            try (Stream<Path> stream = Files.walk(inFolderName)) {
                arrayFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(strExtension))
                    .toList();
            } catch (IOException ei) {
                final String strFeedbackErr = String.format(FILE_FIND_ERR, strExtension, inFolderName);
                LogExposureClass.exposeInputOutputException(strFeedbackErr, Arrays.toString(ei.getStackTrace()));
            }
            return arrayFiles;
        }

        /**
         * Get list of sub-folders from a given folder
         * 
         * @param strFolderName folder name to look into
         * @return List of String
         */
        public static List<String> getSubFoldersFromFolder(final String strFolderName) {
            final String strFeedbackAtmpt = String.format("Will attempt to get all sub-folders from within \"%s\" folder...", strFolderName);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            final List<String> arraySubFolders = new ArrayList<>();
            final Path directory = Paths.get(strFolderName);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
                for (final Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        arraySubFolders.add(entry.toString());
                    }
                }
            } catch (IOException ex) {
                final String strFeedbackErr = String.format("Error encountered when attempting to get sub-folders from %s folder... %s", strFolderName, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.debug(strFeedbackErr);
            }
            return arraySubFolders;
        }

        /**
         * Constructor
         */
        private RetrievingSubClass() {
            // intentionally blank
        }

    }

    /**
     * File Content Reading
     */
    public static final class RetrievingCompactOrRegularFileSubClass {
        /**
         * String constant Minified
         */
        private static final String STR_MINIFIED = "Minified";
        /**
         * String constant PrettyPrint
         */
        private static final String STR_PRTY_PRNT = "PrettyPrint";

        /**
         * Establish pre-extensions for Regular and Compact file name
         * @param strFilePattern file pattern to use
         * @return Properties
         */
        private static Properties establishRegularOrCompactFileName(final String strFilePattern) {
            final Properties propsFile = new Properties();
            propsFile.put(STR_MINIFIED, String.format(strFilePattern, ".min"));
            propsFile.put(STR_PRTY_PRNT, String.format(strFilePattern, ""));
            return propsFile;
        }

        /**
         * read Main configuration file
         * @param strFilePattern file pattern to use
         * @return String
         */
        public static String getJsonConfigurationFile(final String strFilePattern) {
            final Properties propsFile = establishRegularOrCompactFileName(strFilePattern);
            String strFileJson = null;
            final Properties propsMinified = RetrievingSubClass.checkFileExistanceAndReadability(propsFile.getProperty(STR_MINIFIED));
            for(final Entry<Object, Object> eMinified : propsMinified.entrySet()) {
                final boolean isItOk = "OK".equals(eMinified.getKey());
                if (isItOk) {
                    strFileJson = eMinified.getValue().toString();
                } else {
                    final Properties propsPreety = RetrievingSubClass.checkFileExistanceAndReadability(propsFile.getProperty(STR_PRTY_PRNT));
                    for(final Entry<Object, Object> ePreety : propsPreety.entrySet()) {
                        final boolean isItOk2 = "OK".equals(ePreety.getKey());
                        strFileJson = getJsonFileName(isItOk2, ePreety, propsFile);
                    }
                }
            }
            return strFileJson;
        }

        /**
         * Getting JSON file name
         * @param isItOk2 file check result
         * @param ePreety file name
         * @param propsFile file Properties
         * @return String
         */
        private static String getJsonFileName(final boolean isItOk2, final Entry<Object, Object> ePreety, final Properties propsFile) {
            final String strFileJson;
            if (isItOk2) {
                strFileJson = ePreety.getValue().toString();
            } else {
                final String strFeedback = String.format("Configuration file was NOT found (not as %s, nor %s)..."
                    , propsFile.getProperty(STR_MINIFIED, "")
                    , propsFile.getProperty(STR_PRTY_PRNT, ""));
                LogExposureClass.LOGGER.error(strFeedback);
                throw new IllegalArgumentException(strFeedback);
            }
            return strFileJson;
        }

        /**
         * Constructor
         */
        private RetrievingCompactOrRegularFileSubClass() {
            // intentionally blank
        }

    }

    /**
     * Constructor
     */
    private FileOperationsClass() {
        // intentionally blank
    }

}
