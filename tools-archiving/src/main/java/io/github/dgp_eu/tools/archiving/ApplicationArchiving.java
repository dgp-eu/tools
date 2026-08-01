/*
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.archiving;

import io.github.dgp_eu.tools.cli.CommonInteractiveClass;
import io.github.dgp_eu.tools.core.*;
import io.github.dgp_eu.tools.utils.ArchivingClass;
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
    /* default */ static void main( final String[] args ) {
        CommonInteractiveClass.startMeUpWithParameters("logs/DGP-EU_Tools-Archiving-", "/tools-archiving-pom.xml");
        final int intArchExitCode = new CommandLine(new ApplicationArchiving()).execute(args);
        CommonInteractiveClass.shutMeDownWithParameters(intArchExitCode, args[0]);
    }

}
