/*
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.json_split;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;

import io.github.dgp_eu.tools.cli.CommonInteractiveClass;
import io.github.dgp_eu.tools.core.*;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;



/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
            JsonSplit.class
    }
)
public final class ApplicationJsonSplit {

    /**
     * Constructor empty
     */
    private ApplicationJsonSplit() {
        super();
    }

    /**
     * Constructor
     *
     * @param args command-line arguments
     */
    /* default */ static void main(final String... args) {
        CommonInteractiveClass.startMeUpWithParameters("logs/DGP-EU_Tools-JsonSplit-", "/tools-json-split-pom.xml");
        final int intJsonExitCode = new CommandLine(new ApplicationJsonSplit()).execute(args);
        CommonInteractiveClass.shutMeDownWithParameters(intJsonExitCode, args[0]);
    }

}


/**
 * JSON splitter
 */
@CommandLine.Command(name = "JsonSplit", 
                     description = "Splits a given JSON file into multiple smaller files") 
class JsonSplit implements Runnable {
    /**
     * JSON actual file size
     */
    private static long fileSize;
    /**
     * Size limit for split
     */
    private static final long SIZE_THRESHOLD = 5_368_709_120L; // 5GB value see https://convertlive.com/u/convert/gigabytes/to/bytes#5
    /**
     * Size percentage difference between actual & splitSize/SIZE_THRESHOLD
     */
    private static float sizeDifference;
    /**
     * balances threshold size
     */
    private static long sizeThreshold;
    /**
     * adds the options defined in 
     * CommonInteractiveClass.FileNameOptionMixinClass to this command
     */
    @Mixin
    private static final CommonInteractiveClass.InFileNameOptionMixinClass OPT_FILE_NAMES =
            new CommonInteractiveClass.InFileNameOptionMixinClass();
    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderDestinationOptionMixinClass to this command
     */
    @Mixin
    private static final CommonInteractiveClass.FolderDestinationOptionMixinClass OPT_FOLDER_DEST = new CommonInteractiveClass.FolderDestinationOptionMixinClass();
    /**
     * size of Split threshold (optional)
     */
    @CommandLine.Option(
            names = {"-sz", "--splitSize"},
            description = "Threshold size value beyond which split will be performed")
    private static long splitSize;
    /**
     * String for file name
     */
    @CommandLine.Option(
            names = {"-fld", "--field"},
            description = "Field name to use for split and bucketing",
            arity = "1",
            required = true)
    private static String strField;
    /**
     * size of Split threshold (optional)
     */
    @CommandLine.Option(
            names = {"-bl", "--bucketLength"},
            description = "Length of final characters to be overwritten as part of the bucketing logic (use -1 for no bucketing)")
    private static int bucketLength;

    @Override
    public void run() {
        final String[] inFiles = OPT_FILE_NAMES.getInFileNames();
        for (final String strFileName : inFiles) {
            setFileSize(strFileName);
            if (fileSize <= 0) {
                final Properties propertiesReturn = FileOperationsClass.RetrievingSubClass.checkFileExistanceAndReadability(fileSize, strFileName);
                final String strFeedback = String.format("There is something not right with given file name... %s", propertiesReturn);
                LogExposureClass.LOGGER.error(strFeedback);
            } else {
                setSplitSizeThreshold();
                setFileSizeDifferenceCompareToThreshold();
                if (fileSize <= sizeThreshold) {
                    final String strFeedback = String.format("File %s has a size of %s bytes which compare to split file threshold of %s bytes is %s%% smaller, hence split is NOT necessary!", strFileName, fileSize, sizeThreshold, sizeDifference);
                    LogExposureClass.LOGGER.info(strFeedback);
                } else {
                    performJsonSplit(strFileName);
                }
            }
        }
    }

    private static void performJsonSplit(final String strFileName) {
        final String strFeedback = String.format("File %s has a size of %s bytes which compared to split file threshold of %s bytes is %s%% bigger, hence split IS required and will be performed!", strFileName, fileSize, sizeThreshold, Math.abs(sizeDifference));
        LogExposureClass.LOGGER.info(strFeedback);
        JsonSplitClass.setInputJsonFile(strFileName);
        JsonSplitClass.setDestinationFolder(OPT_FOLDER_DEST.getFolderDestination());
        JsonSplitClass.setRelevantField(strField);
        if (bucketLength != 0) {
            JsonSplitClass.setBucketLength(bucketLength);
        }
        final String destPattern = JsonSplitClass.buildDestinationFileName("x").replaceAll("x.json", ".*.json");
        FileOperationsClass.DeletingSubClass.deleteFilesMatchingPatternFromFolder(OPT_FOLDER_DEST.getFolderDestination(), destPattern); // clean slate to avoid inheriting old content
        JsonSplitClass.splitJsonIntoSmallerGrouped(); // actual logic
    }

    /**
     * Setter for fileSize
     */
    private static void setFileSize(final String strFileName) {
        fileSize = FileOperationsClass.RetrievingSubClass.getFileSizeIfFileExistsAndIsReadable(strFileName);
    }

    /**
     * Setter for fileSize
     */
    private static void setFileSizeDifferenceCompareToThreshold() {
        final float sizePercentage = BasicStructuresClass.computePercentageSafely(fileSize, sizeThreshold);
        sizeDifference = (float) new BigDecimal(Double.toString(100 - sizePercentage))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Setter for sizeThreshold
     */
    private static void setSplitSizeThreshold() {
        sizeThreshold = SIZE_THRESHOLD;
        if (splitSize != 0) {
            sizeThreshold = splitSize;
            final String strFeedback = String.format("A custom split size threshold value has been provided %s and will be used which will ignore default value of %s bytes...", splitSize, SIZE_THRESHOLD);
            LogExposureClass.LOGGER.info(strFeedback);
        }
    }

    /**
     * Constructor empty
     */
    protected JsonSplit() {
        // intentionally blank
    }
}
