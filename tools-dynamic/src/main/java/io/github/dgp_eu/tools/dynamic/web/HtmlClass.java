/** Copyright 2026 Daniel-Gheorghe Popiniuc */
package io.github.dgp_eu.tools.dynamic.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.SequencedMap;

import io.github.dgp_eu.tools.core.time.TimingClass;
import io.github.dgp_eu.tools.core.time.ZoneDataServiceClass;
import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.ConfigurationClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.core.ProjectClass;
import io.github.dgp_eu.tools.core.RegularExpressionsClass;

/**
 * HTML generating logic
 */
public final class HtmlClass {

    /**
     * Application Details
     * @return Content
     */
    public static String buildApplicationCopyright() {
        final String prjFirstDeveloper = ProjectClass.getFirstDeveloper();
        return String.format("&copy; by %s", prjFirstDeveloper);
    }

    /**
     * Application Details
     * @return Content
     */
    public static String buildApplicationDetail() {
        final String prjVersion = ProjectClass.getProjectVersion();
        return String.format("%s&trade; v.%s", ProjectClass.getProjectName(), prjVersion);
    }

    /**
     * Geographical Coordinates from TZ
     * @return String
     */
    public static String buildGeographicalCoordinatesFromTimeZone(final String sessionTimeZone) {
        final ZoneDataServiceClass.ZoneInfoRecord zInfo = ZoneDataServiceClass.get(sessionTimeZone);
        return zInfo == null ? "0,0" : zInfo.latitude() + "," + zInfo.longitude();
    }

    /**
     * Building Time-Zone select as String
     * @return String w. TZ select
     */
    public static String buildMenuString(final SequencedMap<String, Map<String, String>> inMapMenu) {
        final StringBuilder strMenuContent = new StringBuilder(1000);
        inMapMenu.forEach((strKey, mapValue) -> {
            if (!mapValue.getOrDefault(ConfigurationClass.STR_MENU, "").isEmpty()) {
                strMenuContent.append(String.format("<li><a href=\"?page=%s\"><i class=\"%s\"></i>%s</a></li>", strKey, mapValue.get(ConfigurationClass.STR_ICON), mapValue.get(ConfigurationClass.STR_MENU)));
            }
        });
        return strMenuContent.toString();
    }

    /**
     * Building Time-Zone select
     * @return Content
     */
    public static String buildTimeZoneSelect(final String inTimeZone) {
        final SequencedMap<String, String> sortedTimeZones = ZoneDataServiceClass.loadSupportedTimeZones();
        final Properties selectProps = new Properties();
        selectProps.put(ConfigurationClass.STR_NAME, "TZ");
        selectProps.put("Id", "TZ");
        selectProps.put(ConfigurationClass.STR_DEFAULT, inTimeZone);
        selectProps.put(ConfigurationClass.STR_SIZE, 1);
        selectProps.put("AutoSubmit", 1);
        return SelectInputSubClass.buildSelectInput(sortedTimeZones, selectProps);
    }

    /**
     * List and Maps management
     */
    public static final class FileInfoSubClass {
        /** Variable for File Modified Time-stamp */
        /* default */ private static String fileModifiedTs = "unknown modified timestamp";
        /** Variable for File size (bytes) */
        /* default */ private static long fileSizeBytes;

        /**
         * constructor
         */
        private FileInfoSubClass() {
            // intentionally left blank
        }

        /**
         * Build Information Box
         * @return String
         */
        public static String gatherFileStatistics(final Path fileName) {
            if (Files.exists(fileName)) {
                fileSizeBytes = fileName.toFile().length();
                fileModifiedTs = TimingClass.LocalizationSubClass.FileSubSubClass.getFileLastModifiedTimeAsHumanReadableFormat(fileName);
            } else {
                final String strFeedback = String.format("Given file %s was not found on disk, hence will be looking for it within JAR", fileName);
                LogExposureClass.LOGGER.debug(strFeedback);
                final String internalFile = fileName.toString().replace("\\", "/");
                try (InputStream inStream = HtmlClass.class.getResourceAsStream(internalFile)) {
                    final String strFeedback2 = String.format("Input Stream is: %s", inStream);
                    LogExposureClass.LOGGER.debug(strFeedback2);
                    fileSizeBytes = inStream.transferTo(OutputStream.nullOutputStream());
                    final URL resourceUrl = HtmlClass.class.getResource(internalFile);
                    final String strFeedback3 = String.format("URI is: %s", resourceUrl);
                    LogExposureClass.LOGGER.debug(strFeedback3);
                    final long lastModified = resourceUrl.openConnection().getLastModified();
                    final ZonedDateTime zonedLastModified = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault());
                    fileModifiedTs = TimingClass.LocalizationSubClass.convertZonedTimestampFriendly(zonedLastModified,
                            TimingClass.DATE_TIME_MS_ABRV).replaceAll(".000$", "");
                } catch (IOException ex) {
                    LogExposureClass.exposeProjectModel(Arrays.toString(ex.getStackTrace()));
                }
            }
            final String rawHtml = "File is <span class=\"importantText\">%s</span>, having as size of <span class=\"importantText\">%s bytes (%s)</span>, last modified time-stamp on <span class=\"importantText\">%s</span>";
            final String strThousandSep = "%,d";
            return String.format(rawHtml,
                    fileName.getFileName().toString(),
                    String.format(Locale.US, strThousandSep, fileSizeBytes),
                    BasicStructuresClass.NumberConversionSubClass.convertUnits(fileSizeBytes, "binary"),
                    fileModifiedTs);
        }

    }

    /**
     * List and Maps management
     */
    public static final class SelectInputSubClass {
        /**
         * Variable for Defaults
         */
        private static List<String> defaults = new ArrayList<>();
        /**
         * Variable for Additional Attributes
         */
        private static String additionalAttrib = "";

        /**
         * build Label as HTML tag 
         * @param objFeatures optional HTML Table features
         * @return String
         */
        private static String buildLabelTag(final Properties objFeatures) {
            final String strLabel = objFeatures.getOrDefault(ConfigurationClass.STR_LABEL, "").toString()
                    + (objFeatures.getOrDefault(ConfigurationClass.STR_MULTIPLE, "").toString().isEmpty() ? "" : "<sup>(multiple values possible)</sup>");
            final String tagLabelRaw = "<label for=\"%s\"%s>%s:</label>";
            final String strLabelStyle = objFeatures.getOrDefault("Label Style", "").toString().isEmpty() ? "" : " style=\"" + objFeatures.get("Label Style").toString() + "\"";
            return String.format(tagLabelRaw, objFeatures.get("Id"), strLabelStyle, strLabel)
                    + (objFeatures.getOrDefault("Label on Same Line", "").toString().isEmpty() ? "<br/>" : "");
        }

        /**
         * establishing the Key to Remember if relevant
         * @param objFeatures optional HTML Table features
         * @return String
         */
        public static String buildSelectInput(final SequencedMap<String, String> mapValues, final Properties objFeatures) {
            final List<String> outHtml = new ArrayList<>();
            if (!objFeatures.getOrDefault(ConfigurationClass.STR_LABEL, "").toString().isEmpty()) {
                outHtml.add(buildLabelTag(objFeatures));
            }
            manageAdditionalAttributesAndDefaults(objFeatures);
            final String strName = objFeatures.get(ConfigurationClass.STR_NAME)
                    + (objFeatures.getOrDefault(ConfigurationClass.STR_MULTIPLE, "").toString().isEmpty() ? "" : "[]");
            outHtml.add(String.format("<select name=\"%s\" id=\"%s\"%s>",
                    strName,
                    objFeatures.get("Id"),
                    additionalAttrib));
            mapValues.forEach((strValue, strText) -> {
                final String strSelected = !defaults.isEmpty() && defaults.contains(strValue) ? " selected" : "";
                outHtml.add(String.format("<option value=\"%s\"%s>%s</option>",
                        strValue,
                        strSelected,
                        strText));
            });
            outHtml.add("</select>");
            return String.join("", outHtml);
        }

        private static void manageAdditionalAttributesAndDefaults(final Properties objFeatures) {
            final String defaultValue = objFeatures.getOrDefault(ConfigurationClass.STR_DEFAULT, "").toString();
            String[] defaultVals = {defaultValue};
            if (!objFeatures.getOrDefault(ConfigurationClass.STR_MULTIPLE, "").toString().isEmpty()) {
                if (!defaultValue.isEmpty()) {
                    defaultVals = defaultValue.split(",");
                }
                additionalAttrib = String.format(" multiple size=\"%s\"", objFeatures.get(ConfigurationClass.STR_MULTIPLE));
            }
            if (!objFeatures.getOrDefault(ConfigurationClass.STR_SIZE, "").toString().isEmpty()) {
                additionalAttrib = String.format(" size=\"%s\"", objFeatures.get(ConfigurationClass.STR_SIZE));
            }
            final String autoSubmit = objFeatures.getOrDefault("AutoSubmit", "").toString();
            if (!autoSubmit.isEmpty()) {
                 additionalAttrib += " onChange=\"javascript:this.form.submit();\"";
            }
            defaults = Arrays.asList(defaultVals);
        }

        /**
         * constructor
         */
        private SelectInputSubClass() {
            // intentionally left blank
        }

    }

    /**
     * List and Maps management
     */
    public static final class TableSubClass {
        /** CSS to align text to right */
        private static final String CSS_TEXT_RIGHT_NW = "text-align:right;white-space:nowrap;";
        /** Minimum string length threshold for time-zone pattern replacement */
        private static final long LARGE_STRING = 20;
        /** Time Zone variable */
        private static String strInTimeZone;
        /** Time Zone variable */
        private static String strOutTimeZone;

        /** Per-call mutable context to avoid shared static state. */
        private static final class TableBuildContextSubClass {
            /** Variable for current Tab */
            private String currentTabValue;
            /** Variable for lines within Table */
            private final List<String> listTableLines = new ArrayList<>();
            /** Variable for last used TabName */
            private String rememberKey;
            /** Variable for counter of Rows */
            private int rowCounter;
            /** Variable for Table Header */
            private String strTableHeader = "";
            /** Variable for counter inclusion (true/false) */
            private boolean useCounter;

            /**
             * ensuring Table Header is appended
             */
            private static void ensureHeaderAppended(final TableBuildContextSubClass tblContext) {
                if (tblContext.listTableLines.isEmpty()) {
                    tblContext.listTableLines.add(tblContext.strTableHeader);
                    tblContext.rowCounter = 0;
                }
            }

            /**
             * final
             */
            private void finish(final TableBuildContextSubClass tblContext) {
                if (!tblContext.strTableHeader.isEmpty()) {
                    tblContext.listTableLines.add("</tbody></table>");
                    if (!tblContext.rememberKey.isEmpty()) {
                        tblContext.listTableLines.add(String.format("</div><!-- %s --></div><!-- tabStandard -->", tblContext.currentTabValue));
                    }
                }
            }

            /**
             * handle Tab switch
             * @param recordMap properties of the record to be transformed into HTML row
             */
            private static void handleTabSwitch(final SequencedMap<Object, Object> recordMap, final TableBuildContextSubClass tblContext) {
                final Object valObj = recordMap.get(tblContext.rememberKey);
                final String valueForTab = valObj == null ? ConfigurationClass.STR_NULL : valObj.toString();
                final String prev = tblContext.currentTabValue == null ? "" : tblContext.currentTabValue;
                if (!valueForTab.equalsIgnoreCase(prev)) {
                    if (tblContext.listTableLines.isEmpty()) {
                        // first tab: open tab container
                        tblContext.listTableLines.add("<div id=\"tabStandard\" class=\"tabber\">");
                    } else if (tblContext.currentTabValue != null) {
                        // close previous tab's table
                        tblContext.listTableLines.add(String.format("</tbody></table></div><!-- %s -->", tblContext.currentTabValue));
                    }
                    // open new tab with header
                    tblContext.listTableLines.add(String.format("<div class=\"tabbertab\" title=\"%s\">%s", valueForTab, tblContext.strTableHeader));
                    tblContext.currentTabValue = valueForTab;
                    tblContext.rowCounter = 0;
                }
            }

            /**
             * process each record
             * @param recordMap map with record content
             */
            private static void processRecord(final SequencedMap<Object, Object> recordMap, final TableBuildContextSubClass tblContext) {
                HeaderSubSubClass.ensureHeaderExists(recordMap, tblContext);
                if (tblContext.rememberKey.isEmpty()) {
                    ensureHeaderAppended(tblContext);
                } else {
                    handleTabSwitch(recordMap, tblContext);
                }
                if (tblContext.useCounter) {
                    tblContext.rowCounter++;
                    recordMap.put("#", String.valueOf(tblContext.rowCounter));
                }
                final String crtRow = RowSubSubSubClass.buildTableBodyRow(recordMap, tblContext);
                tblContext.listTableLines.add(crtRow);
            }

            /**
             * Rows logic
             */
            private static final class RowSubSubSubClass {
                /** Variable for specialValues */
                private static final SequencedMap<String, SpValuesRecord> MAP_SPEC_VALS = new LinkedHashMap<>();
                /** Record for ZoneInfo */
                /* default */ public record SpValuesRecord(
                    String newValue,
                    String newStyle) {}

                static {
                    loadPredefinedValuesAndTheirStyles();
                }

                /**
                 * Table Body row logic
                 * @param recordMap properties of the record to be transformed into HTML row
                 * @return String
                 */
                private static String buildTableBodyRow(final SequencedMap<Object, Object> recordMap, final TableBuildContextSubClass tblContext) {
                    final StringBuilder strTableRow = new StringBuilder(1000);
                    strTableRow.append("<tr>");
                    recordMap.forEach((strKey, objValue) -> {
                        if (!tblContext.rememberKey.equalsIgnoreCase(strKey.toString())
                                && !ConfigurationClass.STR_ROW_STYLE.equalsIgnoreCase(strKey.toString())) {
                            final StringBuilder cellStyle = new StringBuilder(100);
                            if (recordMap.containsKey(ConfigurationClass.STR_ROW_STYLE)) {
                                cellStyle.append(recordMap.get(ConfigurationClass.STR_ROW_STYLE).toString());
                            }
                            final Map<String, String> mapSmartLogic = manageCellStyleAndValue(objValue);
                            final String strValue = mapSmartLogic.get("value");
                            if (!mapSmartLogic.get(ConfigurationClass.STR_STYLE).isEmpty()) {
                                cellStyle.append(mapSmartLogic.get(ConfigurationClass.STR_STYLE));
                            }
                            if (cellStyle.isEmpty()) {
                                strTableRow.append(String.format("<td>%s</td>", strValue));
                            } else {
                                strTableRow.append(String.format("<td style=\"%s\">%s</td>", cellStyle, strValue));
                            }
                        }
                    });
                    strTableRow.append("</tr>");
                    return strTableRow.toString();
                }

                /**
                 * loading special Values and CSS style
                 */
                private static void loadPredefinedValuesAndTheirStyles() {
                    MAP_SPEC_VALS.put(ConfigurationClass.STR_NULL, new SpValuesRecord("&lt;NULL&gt;", "color:LightGrey;font-style:italic;"));
                    MAP_SPEC_VALS.put("", new SpValuesRecord("&lt;blank&gt;", "color:Grey;font-style:italic;"));
                }

                /**
                 * Manage Cell Style and Value
                 * @param inValue input value
                 * @return Map
                 */
                private static Map<String, String> manageCellStyleAndValue(final Object inValue) {
                    String cellStyle = "";
                    String strValue = inValue.toString();
                    final int strLength = strValue.length();
                    if (MAP_SPEC_VALS.containsKey(strValue)) {
                        cellStyle = MAP_SPEC_VALS.get(strValue).newStyle;
                        strValue = MAP_SPEC_VALS.get(strValue).newValue;
                    } else {
                        final String tempValue = transformValueByPatternMatch(strValue);
                        if (tempValue != null) {
                            cellStyle = CSS_TEXT_RIGHT_NW;
                            strValue = tempValue;
                        } else if (strLength >= LARGE_STRING) {
                            strValue = RegularExpressionsClass.replacePatternsWithTimeZones(strValue);
                            cellStyle = strValue.equals(inValue.toString()) ? cellStyle : CSS_TEXT_RIGHT_NW;
                        }
                    }
                    return Map.of(
                            ConfigurationClass.STR_STYLE,
                            cellStyle,
                            "value",
                            strValue);
                }

                /**
                 * Transform 1 value by Given Pattern
                 * @param inputString input value
                 * @param crtPattern matching pattern
                 * @return String
                 */
                private static String transformSingleValueByPattern(final String inputString, final String crtPattern) {
                    return switch (crtPattern) {
                        case "decimal"                          -> BasicStructuresClass.StringTransformationSubClass.formatStringWithDecimalContentWithThousandDecimalSeparator(inputString);
                        case "integer"                          -> String.format(Locale.US, "%,d", BasicStructuresClass.convertStringIntoInteger(inputString));
                        case "long"                             -> String.format(Locale.US, "%,d", BasicStructuresClass.convertStringIntoLong(inputString));
                        case ConfigurationClass.STR_JUST_DATE -> TimingClass.LocalizationSubClass.formatDateFriendly(inputString, TimingClass.ISO_DATE, TimingClass.ISO_DATE_ABRV);
                        case ConfigurationClass.STR_TIMESTAMP -> TimingClass.LocalizationSubClass.convertTimestampFriendly(inputString, TimingClass.DATE_TIME, TimingClass.DATE_TIME_ABRV);
                        case ConfigurationClass.STR_TS_MSEC   -> TimingClass.LocalizationSubClass.convertTimestampFriendly(inputString, TimingClass.DATE_TIME_MS, TimingClass.DATE_TIME_MS_ABRV);
                        case "byteSize", "fullAging"            -> inputString;
                        default                                 -> "";
                    };
                }

                /**
                 * Transform 1 value by Given Pattern
                 * @param inputString input value
                 * @return String
                 */
                private static String transformValueByPatternMatch(final String inputString) {
                    String outputString = null;
                    final List<String> arrayPatterns = List.of("decimal", "integer", "long", ConfigurationClass.STR_TS_MSEC, ConfigurationClass.STR_TIMESTAMP, ConfigurationClass.STR_JUST_DATE, "byteSize", "fullAging");
                    final Iterator<String> itArray = arrayPatterns.iterator();
                    boolean needsToContinue = true;
                    while (itArray.hasNext()
                            && needsToContinue) {
                        final String crtPattern = itArray.next();
                        if (RegularExpressionsClass.ValidationSubClass.isStringActuallySomething(inputString, crtPattern)) {
                            outputString = transformSingleValueByPattern(inputString, crtPattern);
                            needsToContinue = false;
                        }
                    }
                    return outputString;
                }

            }

        }

        /**
         * Generate HTML from a Map of values
         * @param inList values stored as a list
         * @return String
         */
        public static String getListOfSequencedMapIntoHtmlTable(final List<SequencedMap<Object, Object>> inList, final Properties objFeatures) {
            final TableBuildContextSubClass ctx = new TableBuildContextSubClass();
            if (strInTimeZone == null) {
                setInTimeZone(System.getProperty("user.timezone"));
            }
            if (strOutTimeZone == null) {
                setOutTimeZone(System.getProperty("user.timezone"));
            }
            ctx.strTableHeader = "";
            ctx.rememberKey = getRememberKey(objFeatures);
            ctx.useCounter = !objFeatures.getOrDefault(ConfigurationClass.STR_COUNTER, "").toString().isEmpty();
            for (final SequencedMap<Object, Object> recordMap : inList) {
                TableBuildContextSubClass.processRecord(recordMap, ctx);
            }
            ctx.finish(ctx);
            return String.join("", ctx.listTableLines);
        }

        /**
         * establishing the Key to Remember if relevant
         * @param objFeatures optional HTML Table features
         * @return String
         */
        private static String getRememberKey(final Properties objFeatures) {
            String strRememberKey = "";
            if (objFeatures.containsKey(ConfigurationClass.STR_NEW_TAB)) {
                strRememberKey = objFeatures.get(ConfigurationClass.STR_NEW_TAB).toString();
            }
            return strRememberKey;
        }

        /**
         * Setter for strInTimeZone
         * @param inTimeZone input time zone
         */
        public static void setInTimeZone(final String inTimeZone) {
            strInTimeZone = inTimeZone;
            TimingClass.LocalizationSubClass.setInputTimeZone(inTimeZone);
        }

        /**
         * Setter for strInTimeZone
         * @param outTimeZone output time zone
         */
        public static void setOutTimeZone(final String outTimeZone) {
            strOutTimeZone = outTimeZone;
            TimingClass.LocalizationSubClass.setOutputTimeZone(outTimeZone);
        }

        /**
         * Rows logic
         */
        private static final class HeaderSubSubClass {

            /**
             * Table Body row logic
             * @param recordMap properties of the record to be transformed into HTML row
             * @return String
             */
            private static String buildTableHeader(final SequencedMap<Object, Object> recordMap, final TableBuildContextSubClass tblContext) {
                final StringBuilder strBuilder = new StringBuilder(100);
                strBuilder.append("<table><thead>");
                recordMap.forEach((strKey, _) -> {
                    if (!tblContext.rememberKey.equalsIgnoreCase(strKey.toString())
                            && !ConfigurationClass.STR_ROW_STYLE.equalsIgnoreCase(strKey.toString())) {
                        strBuilder.append(String.format("<th>%s</th>", strKey));
                    }
                });
                if (tblContext.useCounter) {
                    strBuilder.append("<th>#</th>");
                }
                strBuilder.append("</thead><tbody>");
                return strBuilder.toString();
            }

            /**
             * initiating Table Header
             * @param recordMap records to parse
             */
            private static void ensureHeaderExists(final SequencedMap<Object, Object> recordMap, final TableBuildContextSubClass tblContext) {
                if (tblContext.strTableHeader.isEmpty()) {
                    tblContext.strTableHeader = buildTableHeader(recordMap, tblContext);
                }
            }

        }

        /**
         * constructor
         */
        private TableSubClass() {
            // intentionally left blank
        }

    }

    /**
     * constructor
     */
    private HtmlClass() {
        // intentionally left blank
    }

}
