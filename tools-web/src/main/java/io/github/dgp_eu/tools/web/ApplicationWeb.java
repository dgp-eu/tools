/**
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.web;

import java.util.Locale;
import java.util.Properties;

import io.github.dgp_eu.tools.cli.CommonInteractiveClass;
import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.core.RegularExpressionsClass;
import io.github.dgp_eu.tools.databases.SpecificSqLiteClass;
import io.github.dgp_eu.tools.undertow.UndertowClass;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

/**
 * Main Command Line
 */
@CommandLine.Command(
        name = "top",
        subcommands = {
                GetRemoteMavenPackageDetails.class,
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
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "GetRemoteMavenPackageDetails",
                     description = "Read Maven package details from central Maven repository")
class GetRemoteMavenPackageDetails implements Runnable {

    @Override
    public void run() {
        // no-op
        final String strPackage = "com.github.oshi:oshi-core-ffm";
        final String strVersion = RemoteInformationRetrievalClass.MavenSubClass.getLatestVersionFromMavenCentralRepository(strPackage);
        final String strFeedback = String.format("For package %s latest version is: %s", strPackage, strVersion);
        LogExposureClass.LOGGER.info(strFeedback);
        final String strWebSite = RegularExpressionsClass.buildCentralMavenRepositoryUniformResourceLocator(strPackage);
        final String[] packageParts = strPackage.split(":");
        final String strRemoteFileUrl = String.format("%s%s/%s-%s.jar", strWebSite, strVersion, packageParts[1], strVersion);
        final String strFeedback2 = String.format("Remote file is: %s", strRemoteFileUrl);
        LogExposureClass.LOGGER.info(strFeedback2);
        final Properties urlAttributes = RemoteInformationRetrievalClass.RequestSubClass.requestHttpFile(strRemoteFileUrl, "AttributesFromHeader");
        final String strFeedback3 = String.format("Retrieved attributes from header are: %s", urlAttributes);
        LogExposureClass.LOGGER.info(strFeedback3);
        final String strChecksumUrl = strRemoteFileUrl + ".sha256";
        final String checksumValue = RemoteInformationRetrievalClass.RequestSubClass.requestHttpFile(strChecksumUrl, BasicStructuresClass.STR_CONTENT).getOrDefault(BasicStructuresClass.STR_CONTENT, "MISSING").toString().trim().toLowerCase(Locale.ENGLISH);
        final String strFeedback4 = String.format("SHA-256 from %s has content: %s", strChecksumUrl, checksumValue);
        LogExposureClass.LOGGER.info(strFeedback4);
    }

    /**
     * Constructor
     */
    protected GetRemoteMavenPackageDetails() {
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
     * adds the options defined in
     * CommonInteractiveClass.LocalDatabaseFileMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.LocalDatabaseFileMixinClass optLocalDbFile = new CommonInteractiveClass.LocalDatabaseFileMixinClass();

    /**
     * adds the options defined in
     * CommonInteractiveClass.PortOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.PortOptionMixinClass optPortNumber = new CommonInteractiveClass.PortOptionMixinClass();

    /**
     * adds the options defined in
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass optFolderNames = new CommonInteractiveClass.FolderNameOptionMixinClass();

    @Override
    public void run() {
        UndertowClass.setWebPort(String.valueOf(optPortNumber.getPortNumber()));
        SpecificSqLiteClass.setInternalDatabase(optLocalDbFile.getLocalDbFile());
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
