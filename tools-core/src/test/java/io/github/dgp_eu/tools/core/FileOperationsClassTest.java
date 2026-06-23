package io.github.dgp_eu.tools.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains unit tests to verify that the methods 
 * in FileOperationsClass work as expected, such as reading 
 * from and writing to files, handling exceptions, 
 * and ensuring data integrity.
 * The tests are designed to cover various scenarios 
 * and edge cases to ensure robustness.</p>
 */
class FileOperationsClassTest {

    @Test
    @DisplayName("getFileSizeIfFileExistsAndIsReadable returns -99 for null filename")
    void testGetFileSizeReturnsNegativeForNullInput() {
        final long result = FileOperationsClass.RetrievingSubClass.getFileSizeIfFileExistsAndIsReadable(null);
        assertEquals(-99L, result, "Null filename should return -99");
    }

    @Test
    @DisplayName("getFileSizeIfFileExistsAndIsReadable returns -3 for non-existent file")
    void testGetFileSizeReturnsDoesNotExistForMissingFile() {
        final String nonExistent = Path.of(System.getProperty("java.io.tmpdir"), "no-such-file-" + System.nanoTime() + ".tmp").toString();
        final long result = FileOperationsClass.RetrievingSubClass.getFileSizeIfFileExistsAndIsReadable(nonExistent);
        assertEquals(-3L, result, "Non-existent file should return -3");
    }

    @Test
    @DisplayName("getFileSizeIfFileExistsAndIsReadable returns -2 for a directory input")
    void testGetFileSizeReturnsNotAFileForDirectory() throws IOException {
        final Path tempDir = Files.createTempDirectory("fileops-test-dir-");
        try {
            final long result = FileOperationsClass.RetrievingSubClass.getFileSizeIfFileExistsAndIsReadable(tempDir.toString());
            assertEquals(-2L, result, "Directory input should return -2");
        } finally {
            Files.walk(tempDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    @Test
    @DisplayName("getFileSizeIfFileExistsAndIsReadable returns actual size for readable file")
    void testGetFileSizeReturnsActualSizeForReadableFile() throws IOException {
        final Path tempFile = Files.createTempFile("fileops-test-file-", ".txt");
        final byte[] content = "hello-fileops".getBytes(StandardCharsets.UTF_8);
        try {
            Files.write(tempFile, content);
            final long result = FileOperationsClass.RetrievingSubClass.getFileSizeIfFileExistsAndIsReadable(tempFile.toString());
            assertEquals(content.length, result, "Readable file should return its byte length");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("checkFileExistanceAndReadability returns OK property for readable file")
    void testCheckFileExistanceAndReadabilityReturnsOKForReadableFile() throws IOException {
        final Path tempFile = Files.createTempFile("fileops-ok-", ".tmp");
        try {
            Files.writeString(tempFile, "payload", StandardCharsets.UTF_8);
            final Properties props = FileOperationsClass.RetrievingSubClass.checkFileExistanceAndReadability(tempFile.toString());
            assertAll("checkFileExistanceAndReadability returns OK property for readable file",
                    () -> assertNotNull(props, "Properties result should not be null"),
                    () -> assertTrue(props.containsKey("OK"), "Properties should contain OK key for readable file"),
                    () -> assertEquals(tempFile.toString(), props.getProperty("OK"), "OK value should be the original file path")
            );
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("getFolderStatisticsRecursive finds files with given extension recursively")
    void testGetFolderStatisticsRecursive() throws IOException {
        final Path baseDir = Files.createTempDirectory("fileops-recursive-");
        try {
            final Path nested = Files.createDirectory(baseDir.resolve("nestedFolder"));
            Files.createFile(nested.resolve("A.java"));
            Files.createFile(nested.resolve("B.java"));
            Files.createFile(nested.resolve("C.txt"));
            final Properties folderProps = FileOperationsClass.StatisticsSubClass .getFolderStatisticsRecursive(nested.toString(), new Properties());
            assertAll("getSpecificFilesFromFolderRecursive finds files with given extension recursively",
                    () -> assertEquals(3L, folderProps.get("TOTAL_OBJECTS"), "Should find exactly 3 objects"),
                    () -> assertEquals(0L, folderProps.get("DIRECTORIES"), "Should find 0 sub-folders"),
                    () -> assertEquals(3L, folderProps.get("FILES"), "Should find exactly 3 .java files")
            );
        } finally {
            Files.walk(baseDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    @DisplayName("getSpecificFilesFromFolderRecursive finds files with given extension recursively")
    void testGetSpecificFilesFromFolderRecursiveFindsFilesWithExtension() throws IOException {
        final Path baseDir = Files.createTempDirectory("fileops-recursive-");
        try {
            final Path nested = Files.createDirectory(baseDir.resolve("nested"));
            Files.createFile(baseDir.resolve("A.java"));
            Files.createFile(nested.resolve("B.java"));
            Files.createFile(baseDir.resolve("C.txt"));
            final List<Path> found = FileOperationsClass.RetrievingSubClass.getSpecificFilesFromFolderRecursive(baseDir, "java");
            final List<String> names = found.stream().map(p -> p.getFileName().toString()).toList();
            assertAll("getSpecificFilesFromFolderRecursive finds files with given extension recursively",
                    () -> assertNotNull(found, "Result should not be null"),
                    () -> assertTrue(names.contains("A.java"), "Should include A.java"),
                    () -> assertTrue(names.contains("B.java"), "Should include B.java from nested folder"),
                    () -> assertFalse(names.contains("C.txt"), "Should not include files with other extensions"),
                    () -> assertEquals(2, names.size(), "Should find exactly two .java files")
            );
        } finally {
            Files.walk(baseDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    /**
     * Constructor
     */
    FileOperationsClassTest() {
        // intentionally blank
    }

}
