/*
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.incubator;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpOption;
import java.net.http.HttpOption.Http3DiscoveryMode;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jspecify.annotations.NonNull;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.core.RegularExpressionsClass;
import io.github.dgp_eu.tools.core.TimingClass;

/**
 * XML management
 */
public final class RemoteInformationRetrievalClass {
    /** HTTP client constant */
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_3)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * building Central Maven Repository as URL
     * @param inPackage input Maven package
     * @return URL to Central Maven Repository
     */
    private static URL buildMavenCentralRepositoryUniformResourceLocatorFromPackage(final String inPackage) {
        final String strWebSite = RegularExpressionsClass.buildCentralMavenRepositoryUniformResourceLocator(inPackage) + "maven-metadata.xml";
        final String strFeedback = String.format("Uniform Resource Locator from Central Maven Repository for %s package is: %s", inPackage, strWebSite);
        LogExposureClass.LOGGER.info(strFeedback);
        return buildUniformResourceLocatorFromString(strWebSite);
    }

    /**
     * build URL from Strin
     * @param strWebSite input URL as String
     * @return URL
     */
    public static URL buildUniformResourceLocatorFromString(@NonNull final String strWebSite) {
        URL urlReturn = null;
        try {
            urlReturn = URI.create(strWebSite).toURL();
        } catch (MalformedURLException e) {
            final String strFeedback = String.format("Malformed Exception encountered on URL as %s", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return urlReturn;
    }

    /**
     * expose to Logs HTTP response version
     * @param response version of used HTTP protocol
     */
    private static void exposeHttpResponseVersion(final HttpResponse<?> response) {
        final String strFeedbackErr = String.format("Response protocol version was %s", response.version().toString());
        LogExposureClass.LOGGER.info(strFeedbackErr);
    }

    /**
     * get latest version if a Maven Package
     * @param inPackage input Maven package
     * @return String as version
     */
    public static String getLatestVersionFromMavenCentralRepository(final String inPackage) {
        final URL url = buildMavenCentralRepositoryUniformResourceLocatorFromPackage(inPackage);
        String strLatestVersion = "";
        assert url != null;
        try (InputStream inStream = url.openStream()) {
            final Document doc = getDocumentFromInputStream(inStream);
            if (doc != null) {
                final Node latest = doc.getElementsByTagName("latest").item(0);
                final Node release = doc.getElementsByTagName("release").item(0);
                strLatestVersion = latest != null ? latest.getTextContent() : "";
                if (strLatestVersion.isBlank()) {
                    strLatestVersion = release != null ? release.getTextContent() : "";
                }
            }
        } catch (IOException e) {
            final String strFeedback = String.format("IO Exception while attempting to read remote XML from an URL as %s", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return strLatestVersion;
    }

    /**
     * get Document from inStream
     * @param inStream Input Stream
     * @return Document
     */
    private static Document getDocumentFromInputStream(final InputStream inStream) {
        Document doc = null;
        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            docBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            docBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            docBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            docBuilderFactory.setExpandEntityReferences(false);
            doc = parseDocumentFromInputStram(inStream, docBuilderFactory);
        } catch (ParserConfigurationException e) {
            final String strFeedback = String.format("Parser Configuration Exception while attempting to read remote XML from an URL as %s", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return doc;
    }

    /**
     * parse Doc from Input Stream
     * @param inStream Input Stream
     * @param docBuilderFactory DocumentBuilderFactory
     * @return Document
     */
    private static Document parseDocumentFromInputStram(final InputStream inStream, final DocumentBuilderFactory docBuilderFactory) {
        Document doc = null;
        try {
            final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            doc = docBuilder.parse(inStream);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            final String strFeedback = String.format("Exception while attempting to read remote XML from an URL... %s", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return doc;
    }

    /**
     * Unified method to handle different remote file requests
     * @param strRemoteFileUrl input remote file URL
     * @param inWhat input Method
     * @return Properties with one or multiple values
     */
    public static Properties requestHttp(final String strRemoteFileUrl, final String inWhat) {
        final Properties fileProperties = new Properties();
        final URI inputUri = URI.create(strRemoteFileUrl);
        try {
            final HttpRequest.Builder builder = HttpRequest.newBuilder(inputUri)
                    .setOption(HttpOption.H3_DISCOVERY, Http3DiscoveryMode.ANY)
                    .timeout(Duration.ofSeconds(10));
            switch(inWhat) {
                case "AttributesFromHeader":
                    final HttpRequest request = builder
                        .method("HEAD", HttpRequest.BodyPublishers.noBody())
                        .build();
                    final HttpResponse<Void> responseHeader = CLIENT
                            .send(request, HttpResponse.BodyHandlers.discarding());
                    exposeHttpResponseVersion(responseHeader);
                    final String lastModified = responseHeader.headers()
                            .firstValue("Last-Modified")
                            .orElse("");
                    if (!lastModified.isBlank() ) {
                        fileProperties.put("Last Modified", TimingClass.LocalizationSubClass.convertDateOrTimestampFormats(lastModified, DateTimeFormatter.RFC_1123_DATE_TIME, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                    }
                    fileProperties.put("Size", responseHeader.headers()
                            .firstValueAsLong("Content-Length")
                            .orElse(-1L));
                    break;
                case BasicStructuresClass.STR_CONTENT:
                    final HttpRequest requestContent = builder
                        .GET()
                        .build();
                    final HttpResponse<String> responseFull = CLIENT
                            .send(requestContent, HttpResponse.BodyHandlers.ofString());
                    exposeHttpResponseVersion(responseFull);
                    final String fileContent = responseFull.body();
                    if (!fileContent.isBlank()) {
                        fileProperties.put(BasicStructuresClass.STR_CONTENT, fileContent);
                    }
                    break;
                default:
                    final String strFeedbackErr = LogExposureClass.getUnsupportedFeatures(inWhat, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                    throw new UnsupportedOperationException(strFeedbackErr);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            final String strFeedback = String.format("Execution was interrupted... %s", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.warn(strFeedback);
        } catch (IOException e) {
            final String strFeedback = String.format("Input/Output Exception while attempting to read remote XML from an URL as %s", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return fileProperties;
    }

    // Private constructor to prevent instantiation
    private RemoteInformationRetrievalClass() {
        // intentionally blank
    }

}
