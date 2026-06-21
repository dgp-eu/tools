/*
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */ 
package io.github.dgp_eu.tools.environment;

import io.github.dgp_eu.tools.cli.CommonInteractiveClass;
import io.github.dgp_eu.tools.core.FileOperationsClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
            CaptureEnvironmentDetailsIntoJsonFile.class
    }
)
public class ApplicationEnvironment {

    /**
     * Application logic
     * @param args input arguments
     */
    public static void main( String[] args ) {
        CommonInteractiveClass.startMeUpWithParameters("logs/DGP-EU_Tools-Environment-", "/tools-environment-pom.xml");
        final int intEnvExitCode = new CommandLine(new ApplicationEnvironment()).execute(args);
        CommonInteractiveClass.shutMeDownWithParameters(intEnvExitCode, args[0]);
    }

    /** Constructor */
    private ApplicationEnvironment() {
        super();
    }

}

/**
 * Captures execution environment details into Log file
 */
@CommandLine.Command(name = "CaptureEnvironmentDetailsIntoJsonFile",
                     description = "Captures execution environment details into Log file")
class CaptureEnvironmentDetailsIntoJsonFile implements Runnable {
    /**
     * adds the options defined in 
     * CommonInteractiveClass.OutFileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.OutFileNameOptionMixinClass optionOut = new CommonInteractiveClass.OutFileNameOptionMixinClass();

    @Override
    public void run() {
        final String strEnvDetails = EnvironmentCapturingAssembleClass.packageCurrentEnvironmentDetailsIntoJson();
        final String strOutFileName = optionOut.getOutFileName();
        final String strFeedback = String.format("Environment details are %s and will intend to write it to %s file", strEnvDetails, strOutFileName);
        LogExposureClass.LOGGER.info(strFeedback);
        FileOperationsClass.ContentWritingSubClass.writeRawTextToFile(strOutFileName, strEnvDetails);
    }

    /**
     * Private constructor to prevent instantiation
     */
    protected CaptureEnvironmentDetailsIntoJsonFile() {
        super();
    }

}
