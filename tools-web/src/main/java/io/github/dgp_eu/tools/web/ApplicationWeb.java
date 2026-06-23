/**
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.web;

import io.github.dgp_eu.tools.cli.CommonInteractiveClass;
import io.github.dgp_eu.tools.core.*;
import io.github.dgp_eu.tools.databases.SpecificSqLiteSubClass;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

/**
 * Main Command Line
 */
@CommandLine.Command(
        name = "top",
        subcommands = {
                WebUserInterface.class
        }
)
public final class ApplicationWeb {

    /**
     * Constructor
     *
     * @param args command-line arguments
     */
    /* default */ static void main(final String... args) {
        CommonInteractiveClass.startMeUpWithParameters("logs/DGP-EU_Tools-Web-", "/tools-web-pom.xml");
        final int intWebExitCode = new CommandLine(new ApplicationWeb()).execute(args);
        CommonInteractiveClass.shutMeDownWithParameters(intWebExitCode, args[0]);
    }

    /** Constructor */
    private ApplicationWeb() {
        super();
    }

}


/**
 * Supports web interface
 */
@CommandLine.Command(
        name = "WebUserInterface",
        description = "Initiate Web User Interface")
class WebUserInterface implements Runnable {

    /**
     * String for Database
     */
    @CommandLine.Option(
            names = {"-dbr", "--databaseReleases"},
            description = "Database Name with Releases",
            arity = BasicStructuresClass.ARITY_ONLY_ONE,
            required = true
    )
    private static String strDbReleases;

    /**
     * String for Database
     */
    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "Port Number for web user interface",
            arity = BasicStructuresClass.ARITY_ONLY_ONE,
            required = true
    )
    private static long portNumber;

    /**
     * adds the options defined in
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass optFolderNames = new CommonInteractiveClass.FolderNameOptionMixinClass();

    @Override
    public void run() {
        UndertowClass.setWebPort(String.valueOf(portNumber));
        SpecificSqLiteSubClass.setInternalDatabase(strDbReleases);
        WebClass.SoftwareReleasesSubClass.setReleasesDatabase(strDbReleases);
        WebClass.setFolderNamesForChecksumExposure(optFolderNames.getFolderNames());
        UndertowClass.setRootHandler(WebClass.handleWebContent());
        UndertowClass.runWebServer();
    }

    /**
     * Constructor
     */
    protected WebUserInterface() {
        // intentionally blank
    }
}
