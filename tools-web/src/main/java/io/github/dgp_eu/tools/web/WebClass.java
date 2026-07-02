/*
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.web;

import gg.jte.TemplateEngine;
import gg.jte.output.Utf8ByteOutput;
import io.github.dgp_eu.tools.core.*;
import io.github.dgp_eu.tools.databases.DatabaseOperationsClass;
import io.github.dgp_eu.tools.databases.SpecificSqLiteClass;
import io.github.dgp_eu.tools.environment.EnvironmentCapturingAssembleClass;
import io.github.dgp_eu.tools.undertow.HtmlClass;
import io.github.dgp_eu.tools.undertow.UndertowClass;
import io.undertow.server.HttpHandler;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

/**
 * Web interface class
 */
public final class WebClass {
    /** Constant for "Software Releases" */
    public static final String STR_SOFT_RELEASES = "Software Releases";
    /**
     * Menu
     */
    private static final SequencedMap<String, Map<String, String>> MAP_MENU = Stream.of(
            Map.entry("home", Map.of(
                    BasicStructuresClass.STR_ICON, "fa-solid fa-house-user",
                    BasicStructuresClass.STR_MENU, "HomePage",
                    BasicStructuresClass.STR_TITLE, "HomePage")),
            Map.entry(BasicStructuresClass.STR_SOFTWARE_RLS, Map.of(
                    BasicStructuresClass.STR_ICON, "fa-brands fa-dev",
                    BasicStructuresClass.STR_MENU, STR_SOFT_RELEASES,
                    BasicStructuresClass.STR_TITLE, STR_SOFT_RELEASES)),
            Map.entry(BasicStructuresClass.STR_TS, Map.of(
                    BasicStructuresClass.STR_ICON, "fa-solid fa-square-poll-horizontal",
                    BasicStructuresClass.STR_MENU, "SQLite Table Statistics",
                    BasicStructuresClass.STR_TITLE, "SQLite Table Statistics")),
            Map.entry(BasicStructuresClass.STR_FILE_HASHING, Map.of(
                    BasicStructuresClass.STR_ICON, "fa-solid fa-hashtag",
                    BasicStructuresClass.STR_MENU, "Downloads File Hashing",
                    BasicStructuresClass.STR_TITLE, "Downloads File Hashing")),
            Map.entry(BasicStructuresClass.STR_ENV_DTLS, Map.of(
                    BasicStructuresClass.STR_ICON, "fa-solid fa-computer",
                    BasicStructuresClass.STR_MENU, "Environment Details",
                    BasicStructuresClass.STR_TITLE, "Environment Details"))
    ).collect(
            Collectors.toMap(
                    Map.Entry::getKey, 
                    Map.Entry::getValue, 
                    (v1, _) -> v1, 
                    LinkedHashMap::new)  // Ensures it returns a SequencedMap
    );
    /** Variable for Folders relevant for Checksum Exposure */
    private static String[] strFolderNames;

    /**
     * Outputs file statistics into an HTML table
     * @return String
     */
    public static String getEnvironmentDetailsAsHtmlTable() {
        final Properties objFeatures = new Properties();
        objFeatures.put(BasicStructuresClass.STR_NEW_TAB, "Category");
        final List<Properties> envDetails = EnvironmentCapturingAssembleClass.packageCurrentEnvironmentDetailsIntoListOfProperties();
        final List<String> desiredOrder = List.of("Category", "Element", "Value");
        final List<SequencedMap<Object, Object>> orderedList = envDetails.stream()
                .map(prop -> BasicStructuresClass.ListAndMapSubClass.sortProperties(prop, desiredOrder))
                .toList();
        return HtmlClass.TableSubClass.getListOfSequencedMapIntoHtmlTable(orderedList, objFeatures);
    }

    /**
     * Outputs file statistics into an HTML table
     * @return String
     */
    private static String getFileHashingAsHtmlTable() {
        final String[] inAlgorithms = {"SHA-256"};
        FileOperationsClass.StatisticsSubClass.setChecksumAlgorithms(inAlgorithms);
        final List<Properties> foldersStatistics = new ArrayList<>();
        for(final String crtFolderName: strFolderNames) {
            final List<Properties> crtFileStatistics = FileOperationsClass.StatisticsSubClass.getFileStatisticsIntoListOfProperties(crtFolderName);
            foldersStatistics.addAll(crtFileStatistics);
        }
        final List<String> desiredOrder = List.of("Folder", "File", "Size [bytes]", "Last Modified Timestamp", "SHA-256");
        final List<SequencedMap<Object, Object>> orderedList = foldersStatistics.stream()
                .map(prop -> BasicStructuresClass.ListAndMapSubClass.sortProperties(prop, desiredOrder))
                .toList();
        return HtmlClass.TableSubClass.getListOfSequencedMapIntoHtmlTable(orderedList, new Properties());  
    }

    /**
     * Getter for MAP_MENU
     * @return SequencedMap
     */
    public static SequencedMap<String, Map<String, String>> getMenu() {
        return MAP_MENU;
    }

    /**
     * expose Software Release details from internal DB
     * @return String software releases details
     */
    private static String getSoftwareReleasesIntoHtmlTable() {
        final Properties objFeatures = new Properties();
        objFeatures.put(BasicStructuresClass.STR_NEW_TAB, "Profile");
        final List<Properties> softwareReleases = SoftwareReleasesSubClass.consolidateSoftwareReleases();
        final List<String> desiredOrder = List.of("Organization", "Product", "Version", "Date", "Files");
        final List<SequencedMap<Object, Object>> orderedList = softwareReleases.stream()
                .map(prop -> BasicStructuresClass.ListAndMapSubClass.sortProperties(prop, desiredOrder))
                .toList();
        return HtmlClass.TableSubClass.getListOfSequencedMapIntoHtmlTable(orderedList, objFeatures);
    }

    /**
     * Body content handler
     * @return web Content
     */
    public static gg.jte.Content handleBodyContent() {
        final String page = UndertowClass.ParametersSubClass.getPageParameter();
        final String strSqLiteInfoBox = HtmlClass.buildFileInfoBox(Path.of(SpecificSqLiteClass.getInternalDatabase()));
        return output -> output.writeContent(switch(page) {
            case BasicStructuresClass.STR_ENV_DTLS      -> getEnvironmentDetailsAsHtmlTable()
                    + HtmlClass.buildFileInfoBox(Path.of(ProjectClass.getPomFile()));
            case BasicStructuresClass.STR_FILE_HASHING  -> getFileHashingAsHtmlTable();
            case BasicStructuresClass.STR_SOFTWARE_RLS  -> getSoftwareReleasesIntoHtmlTable()
                    + strSqLiteInfoBox;
            case BasicStructuresClass.STR_TS            -> HtmlClass.TableSubClass.getListOfSequencedMapIntoHtmlTable(
                    SpecificSqLiteClass.SqLiteStatisticsSubClass.getTableStatisticsIntoListForHtmlTable(), new Properties())
                    + strSqLiteInfoBox;
            default                                     -> String.format("Welcome %s", System.getProperty("user.name"));
        });
    }

    /**
     * Handle web content
     * @return PathHandler web content
     */
    public static HttpHandler handleWebContent() {
        return exchange -> {
            UndertowClass.handleCommonThings(exchange);
            final String page = UndertowClass.ParametersSubClass.getPageParameter();
            switch(page) {
                case BasicStructuresClass.STR_FILE_HASHING,
                        BasicStructuresClass.STR_SOFTWARE_RLS,
                        BasicStructuresClass.STR_TS:
                    TimingClass.LocalizationSubClass.setInputTimeZone("UTC");
                    break;
                default:
                    // intentionally blank
                    break;
            }
            final TemplateEngine templateEngine = UndertowClass.createTemplateEngine();
            final Utf8ByteOutput output = new Utf8ByteOutput();
            UndertowClass.TemplateRenderingSubClass.setOutput(output);
            UndertowClass.TemplateRenderingSubClass.setServerExchange(exchange);
            packAllParameters();
            UndertowClass.TemplateRenderingSubClass.renderTemplate(templateEngine, "index.jte");
        };
    }

    /**
     * Packing all parameters to Template
     */
    private static void packAllParameters() {
        final String page = UndertowClass.ParametersSubClass.getPageParameter();
        UndertowClass.TemplateRenderingSubClass.packParameter("page", page);
        String title = page;
        if (!BasicStructuresClass.STR_LOCALIZATION.equalsIgnoreCase(page)) {
            final Map<String, String> menuEntry = MAP_MENU.get(page);
            title = menuEntry != null ? menuEntry.getOrDefault(BasicStructuresClass.STR_TITLE, page) : page;
        }
        UndertowClass.TemplateRenderingSubClass.packParameter("title", title);
        final gg.jte.Content myMenu = output -> output.writeContent(HtmlClass.buildMenuString(MAP_MENU));
        UndertowClass.TemplateRenderingSubClass.packParameter("menu", myMenu);
        UndertowClass.TemplateRenderingSubClass.packParameter("content", handleBodyContent());
        UndertowClass.TemplateRenderingSubClass.packCommonParameters();
    }

    /**
     * Setter for strFolderNames
     * @param inFolderNames list of Folders relevant for checksum exposure
     */
    public static void setFolderNamesForChecksumExposure(@NonNull final String... inFolderNames) {
        strFolderNames = inFolderNames;
    }

    /**
     * Handling Software releases logic
     */
    public static final class SoftwareReleasesSubClass {
        /**
         * Internal database name
         */
        private static String releasesDatabase;

        /**
         * expose Software Release details from internal DB
         * @return List software releases details
         */
        public static List<Properties> consolidateSoftwareReleases() {
            final List<Properties> softwareReleases = new ArrayList<>();
            final List<Properties> resultReleases = getSoftwareReleasesFromDatabase();
            resultReleases.forEach( recordProperties -> {
                final Properties newProperties = new Properties();
                newProperties.put("Organization", String.format("%s<div style=\"text-align:right;\">[%s]</div>", recordProperties.get("OrganizationName"), recordProperties.get("OrganizationId")));
                newProperties.put("Product", String.format("<a href=\"%s\" target=\"_blank\"><span style=\"float:left;\">%s<br/>[%s]</span><span style=\"float:right;text-align:right;\">%s<br/>[%s]</span></a>", recordProperties.get("Releases"), recordProperties.get("ProductName"), recordProperties.get("ProductId"), recordProperties.get("BranchName"), recordProperties.get("BranchId")));
                newProperties.put("Version", String.format("%s<div style=\"text-align:right;\">[%s]</div>", recordProperties.get("Latest release version"), recordProperties.get("VersionId")));
                final String agingDays = recordProperties.get("Latest release aging days").toString().replaceAll("\\.0$"
                        , "");
                newProperties.put("Date", String.format("%s<br>==> %s", recordProperties.get("Latest release date"), recordProperties.get("Latest release aging full").toString()));
                newProperties.put("Files", String.format("%s [%s]<br/>==> %s [%s]", recordProperties.get("File Kit Name"), recordProperties.get("File Kit Id"), recordProperties.get("File Installed Name"), recordProperties.get("File Installed Id")));
                newProperties.put("Profile", recordProperties.get("Profile Name"));
                newProperties.put(BasicStructuresClass.STR_ROW_STYLE, establishRowStyle(agingDays));
                softwareReleases.add(newProperties);
            });
            return softwareReleases;
        }

        /**
         * Row Style logic
         * @param agingDays number of days
         * @return String row style
         */
        private static String establishRowStyle(final String agingDays) {
            String strRowColor = "#fff";
            if (!agingDays.isEmpty()) {
                final long[] longRanges = {14, 30, 90};
                final String cleanedAging = agingDays.replaceAll(".0$", "");
                final long longAging = BasicStructuresClass.convertStringIntoLong(cleanedAging);
                if (longAging <= longRanges[0]) {
                    strRowColor = "#51ff6d";
                } else if (longAging <= longRanges[1]) {
                    strRowColor = "#ccffe8";
                } else if (longAging <= longRanges[2]) {
                    strRowColor = "#fdffcc";
                }
            }
            return String.format("background-color:%s;", strRowColor);
        }

        /**
         * expose Software Release details from internal DB
         * @return List software releases details
         */
        private static List<Properties> getSoftwareReleasesFromDatabase() {
            List<Properties> resultReleases = new ArrayList<>();
            try (Connection objConnection = SpecificSqLiteClass.getSqLiteConnection(releasesDatabase);
                    Statement objStatement = DatabaseOperationsClass.ConnectivitySubClass.createSqlStatement(BasicStructuresClass.STR_SQLITE, objConnection)) {
                final Properties rsProperties = new Properties();
                rsProperties.put("Purpose", STR_SOFT_RELEASES);
                rsProperties.put("QueryToUse", DatabaseOperationsClass.getPreDefinedQuery(BasicStructuresClass.STR_SQLITE, "ReleasesListProductBranches"));
                rsProperties.put("FetchType", "Values");
                resultReleases = DatabaseOperationsClass.ResultSettingSubClass.getResultSetStandardized(objStatement, rsProperties, new Properties());
            } catch (SQLException e) {
                final String strFeedbackErr = String.format("%s connection has failed %s", BasicStructuresClass.STR_SQLITE, e.getLocalizedMessage());
                LogExposureClass.LOGGER.debug(strFeedbackErr);
            }
            return resultReleases;
        }

        /**
         * Setter for releasesDatabase
         */
        public static void setReleasesDatabase(final String inDatabase) {
            releasesDatabase = inDatabase;
        }

        // Private constructor to prevent instantiation
        private SoftwareReleasesSubClass() {
            // intentional empty
        }

    }

    private WebClass() {
        // intentionally blank
    }

}
