package io.github.dgp_eu.tools.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import io.github.dgp_eu.tools.core.time.TimingClass;
import io.github.dgp_eu.tools.core.time.TimingClass.AgingSubClass;

/**
 * Statistics
 */
public final class FileStatisticsClass {
    /** Checksum algorithms */
    private static String[] listAlgorithms = {"SHA-256", "SHA-512", "SHA3-256", "SHA3-512"};
    /** file statistics */
    private static final List<Properties> FILE_STATISTICS = new ArrayList<>();

    /**
     * A simple record to hold our results
     */
    /* default */ record FolderStatsRecord(long fileCount, long folderCount, long totalSize) {
        /* default */ static FolderStatsRecord empty() { return new FolderStatsRecord(0, 0, 0); }
        /* default */ FolderStatsRecord add(final FolderStatsRecord other) {
            return new FolderStatsRecord(
                this.fileCount + other.fileCount,
                this.folderCount + other.folderCount,
                this.totalSize + other.totalSize
            );
        }
    }

    /**
     * Get statistics for all files within a given folder
     * @param strFolderName input folder name
     */
    public static void captureFileStatisticsFromFolder(final String strFolderName, final String outCsvFile) {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outCsvFile), StandardCharsets.UTF_8)) {
            writer.write("Folder;File;Size;Last Modified Time");
            for(final String crtAlgo: listAlgorithms) {
                writer.write(';' + crtAlgo);
            }
            writer.newLine();
            gatherFileStatisticsFromFolderIntoFile(strFolderName, writer);
        } catch (IOException ei) {
            LogExposureClass.exposeInputOutputException(Arrays.toString(ei.getStackTrace()));
        }
    }

    /**
     * Compute Digest for checksum Algorithm
     * @param algorithm input value
     * @return Digest
     */
    private static MessageDigest computeDigestForAlgorithm(final String algorithm) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            final String strFeedbackErr = String.format("Checksum algorithm %s is not available.... %s", algorithm, Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
        return digest;
    }

    /**
     * Compute checksum for a given file
     * @param file input file
     * @param algorithm checksum algorithm name
     * @return String
     */
    public static String computeSingleChecksum(final Path file, final String algorithm) {
        String sbChecksumValue = "checksum not computed";
        try (InputStream istrmFile = Files.newInputStream(file)) {
            sbChecksumValue = computeSingleChecksumFromInputStream(istrmFile, algorithm);
        } catch (IOException e) {
            final String strFeedbackErr = String.format("Error when attempting to get content from file \"%s\": " +
                            "%s", file, Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
        return sbChecksumValue;
    }

    /**
     * Compute Checksum from Input Stream
     * @param inStream input stream
     * @param algorithm algorithm to use for checksum calculation
     * @return Checksum value
     */
    public static String computeSingleChecksumFromInputStream(final InputStream inStream, final String algorithm) {
        final MessageDigest digest = computeDigestForAlgorithm(algorithm);
        final StringBuilder sbChecksumValue = new StringBuilder();
        try (DigestInputStream dis = new DigestInputStream(inStream, digest)) {
            // Read and discard all data while updating the digest
            dis.transferTo(OutputStream.nullOutputStream());
            assert digest != null;
            final byte[] hashBytes = digest.digest();
            for (final byte byteVar : hashBytes) {
                sbChecksumValue.append(String.format("%02x", byteVar));
            }
        } catch (IOException e) {
            final String strFeedbackErr = String.format("Error when attempting to get content from input stream " +
                    "\"%s\": %s", "*", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
        return sbChecksumValue.toString();
    }

    /**
     * Compute all known checksums for a given file
     * @param file input file
     * @return Properties checksum values
     */
    private static Properties computeFileMultipleChecksumsIntoProperties(final Path file) {
        final Properties fileProperties = new Properties();
        for (final String algo : listAlgorithms) {
            fileProperties.put(algo, computeSingleChecksum(file, algo));
        }
        return fileProperties;
    }

    /**
     * performs statistics for all files within a given folder
     * @param strFolderName input folder name
     */
    private static void gatherFileStatisticsFromFolder(final String strFolderName, final ZonedDateTime inRefTimeStamp) {
        final Path folder = Paths.get(strFolderName);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (final Path file : stream) {
                if (Files.isDirectory(file)) {
                    gatherFileStatisticsFromFolder(file.toString(), inRefTimeStamp);
                } else if (Files.isRegularFile(file)) {
                    FILE_STATISTICS.add(getSingleFileStatistic(file, inRefTimeStamp));
                }
            }
        } catch (IOException ei) {
            final String strFeedback = String.format("I/O exception on processing %s folder...", strFolderName);
            LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
        }
    }

    /**
     * performs statistics for all files within a given folder
     * @param strFolderName input folder name
     */
    private static void gatherFileStatisticsFromFolderIntoFile(final String strFolderName, final BufferedWriter writer) {
        final ZonedDateTime refTimeStamp = TimingClass.getCurrentZonedDateTime();
        final List<Properties> crtFileStatistics = getFileStatisticsIntoListOfProperties(strFolderName, refTimeStamp);
        crtFileStatistics.forEach(fileProperties -> {
            try {
                writer.write(fileProperties.get("Folder").toString()
                        + ';' + fileProperties.get("File").toString()
                        + ';' + fileProperties.get(ConfigurationClass.STR_SIZE).toString()
                        + ';' + fileProperties.get("Last Modified Time").toString());
                for (final String algo : listAlgorithms) {
                    writer.write(';' + fileProperties.get(algo).toString());
                }
                writer.newLine();
            } catch (IOException ei) {
                final String strFeedback = "Error writing files statistics";
                LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
            }
        });
    }

    /**
     * performs statistics for all files within a given folder
     * @param strFolderName input folder name
     */
    public static List<Properties> getFileStatisticsIntoListOfProperties(final String strFolderName, final ZonedDateTime inRefTimeStamp) {
        TimingClass.LocalizationSubClass.setReferenceTimeStampValueForAgingCalculation(inRefTimeStamp);
        if (!FILE_STATISTICS.isEmpty()) {
            FILE_STATISTICS.clear();
        }
        gatherFileStatisticsFromFolder(strFolderName, inRefTimeStamp);
        return FILE_STATISTICS;
    }

    /**
     * get Folder statistics recursively
     * @param strFolderName folder name
     * @param pathProps path properties
     * @return Properties
     */
    public static Properties getFolderStatisticsRecursive(final String strFolderName, final Properties pathProps) {
        final Path directory = Paths.get(strFolderName.replace("\"", ""));
        // use DirectoryStream to list files which are present in specific
        try (Stream<Path> stream = Files.walk(directory)) {
            final FolderStatsRecord stats = stream
                .map(path -> {
                    if (Files.isDirectory(path)) {
                        // Don't count the root directory itself as a sub-folder
                        return path.equals(directory) ? FolderStatsRecord.empty() : new FolderStatsRecord(0, 1, 0);
                    } else {
                        try {
                            return new FolderStatsRecord(1, 0, Files.size(path));
                        } catch (IOException e) {
                            final String strFeedback = String.format("Input/Output exception on %s folder encountered on %s", strFolderName, Arrays.toString(e.getStackTrace()));
                            LogExposureClass.LOGGER.debug(strFeedback);
                            return FolderStatsRecord.empty();
                        }
                    }
                })
                .reduce(FolderStatsRecord.empty(), FolderStatsRecord::add);
            pathProps.put("TOTAL_OBJECTS", stats.folderCount() + stats.fileCount());
            pathProps.put("DIRECTORIES", stats.folderCount());
            pathProps.put("FILES", stats.fileCount());
            pathProps.put("SIZE_BYTES", stats.totalSize());
        } catch (IOException ei) {
            final Path foderName = Path.of(strFolderName);
            final String strFeedback = String.format(FileOperationsClass.FILE_FIND_ERR, foderName.getParent(), foderName.getFileName());
            LogExposureClass.exposeInputOutputException(strFeedback,
                    Arrays.toString(ei.getStackTrace()));
        }
        return pathProps;
    }

    /**
     * Determining single file statistics
     * @param file in scope
     * @return Properties with relevant statistics
     */
    private static Properties getSingleFileStatistic(final Path file, final ZonedDateTime inRefTimeStamp) {
        final String strFeedback = String.format("Will process file %s for multiple statistics", file);
        LogExposureClass.LOGGER.debug(strFeedback);
        final Properties fileProperties = new Properties();
        fileProperties.put("Folder", file.getParent().toString());
        fileProperties.put("File", file.getFileName().toString());
        final long fileSize = file.toFile().length();
        fileProperties.put("Size [bytes]", fileSize);
        final String fileSizeDynamic = BasicStructuresClass.NumberConversionSubClass.convertUnits(fileSize, "binary");
        fileProperties.put(ConfigurationClass.STR_SIZE, fileSizeDynamic);
        final ZonedDateTime zFileTimeStamp = TimingClass.LocalizationSubClass.getFileLastModifiedZonedDateTime(file);
        fileProperties.put("Last Modified Timestamp", TimingClass.LocalizationSubClass.convertZonedTimestampFriendly(zFileTimeStamp,
                TimingClass.DATE_TIME_MS_ABRV));
        final String lastModifAging = AgingSubClass.computeAgingIntoHumanReadableWords(zFileTimeStamp, inRefTimeStamp);
        fileProperties.put("Last Modified Aging", lastModifAging);
        fileProperties.putAll(computeFileMultipleChecksumsIntoProperties(file));
        return fileProperties;
    }

    /**
     * Setter for checksum algorithms
     * @param inAlgorithms char
     */
    public static void setChecksumAlgorithms(@NonNull final String... inAlgorithms) {
        listAlgorithms = inAlgorithms;
    }

    /**
     * Constructor
     */
    private FileStatisticsClass() {
        // intentionally blank
    }
}