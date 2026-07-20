/** Copyright 2026 Daniel-Gheorghe Popiniuc */
package io.github.dgp_eu.tools.undertow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.SequencedMap;

import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.FileOperationsClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.core.ProjectClass;
import io.github.dgp_eu.tools.core.RegularExpressionsClass;
import io.github.dgp_eu.tools.core.TimingClass;
import io.github.dgp_eu.tools.core.ZoneDataServiceClass;

/**
 * HTML generating logic
 */
public final class HtmlClass {
    /**
     * Important string template variable
     */
    public static final String STRING_IMPORTANT = "<span style=\"font-weight:bold;text-shadow: 1px 1px 2px #999;\">%s</span>";

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
     * Build Information Box
     * @return String
     */
    public static String buildFileInfoBox(final Path fileName) {
        final String strThousandSep = "%,d";
        String fileSize = "unknown size";
        String fileModified = "unknown modified timestamp";
        String fileChecksum = "unknown SHA-256 checksum";
        if (Files.exists(fileName)) {
            fileSize = String.format(Locale.US, strThousandSep, fileName.toFile().length());
            fileModified = TimingClass.LocalizationSubClass.FileSubSubClass.getFileLastModifiedTimeAsHumanReadableFormat(fileName);
            fileChecksum = FileOperationsClass.StatisticsSubClass.computeSingleChecksum(fileName, "SHA-256");
        } else {
            final String strFeedback = String.format("Given file %s was not found on disk, hence will be looking for it within JAR", fileName);
            LogExposureClass.LOGGER.debug(strFeedback);
            final String internalFile = fileName.toString().replace("\\", "/");
            try (InputStream inStream = HtmlClass.class.getResourceAsStream(internalFile)) {
                final String strFeedback2 = String.format("Input Stream is: %s", inStream);
                LogExposureClass.LOGGER.debug(strFeedback2);
                assert inStream != null;
                fileSize = String.format(Locale.US, strThousandSep, inStream.transferTo(OutputStream.nullOutputStream()));
                final URL resourceUrl = HtmlClass.class.getResource(internalFile);
                final String strFeedback3 = String.format("URI is: %s", resourceUrl);
                LogExposureClass.LOGGER.debug(strFeedback3);
                assert resourceUrl != null;
                final long lastModified = resourceUrl.openConnection().getLastModified();
                final ZonedDateTime zonedLastModified = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault());
                fileModified = TimingClass.LocalizationSubClass.convertZonedTimestampFriendly(zonedLastModified,
                        TimingClass.DATE_TIME_MS_ABRV).replaceAll(".000$", "");
                fileChecksum = FileOperationsClass.StatisticsSubClass.computeSingleChecksumFromInputStream(inStream, "SHA-256");
            } catch (IOException ex) {
                LogExposureClass.exposeProjectModel(Arrays.toString(ex.getStackTrace()));
            }
        }
        final String strTemplate = """
<div class="infoBox" style="box-shadow: 0px 0px 3px 3px blue;font-size:0.7rem;padding:2px;text-align:left;">
    File is %s, having as size of %s bytes, last modified time-stamp on %s with a checksum SHA-256 value of %s
</div>
""";
        return String.format(strTemplate,
                buildStringImportant(fileName.toString()),
                buildStringImportant(fileSize),
                buildStringImportant(fileModified),
                String.format(STRING_IMPORTANT.replace(";\"", ";font-size:0.7rem;\""), fileChecksum));
    }

    /**
     * Building Time-Zone select as String
     * @return String w. TZ select
     */
    public static String buildMenuString(final SequencedMap<String, Map<String, String>> inMapMenu) {
        final StringBuilder strMenuContent = new StringBuilder(1000);
        inMapMenu.forEach((strKey, mapValue) -> {
            if (!mapValue.getOrDefault(BasicStructuresClass.STR_MENU, "").isEmpty()) {
                strMenuContent.append(String.format("<li><a href=\"?page=%s\"><i class=\"%s\"></i>%s</a></li>", strKey, mapValue.get(BasicStructuresClass.STR_ICON), mapValue.get(BasicStructuresClass.STR_MENU)));
            }
        });
        return strMenuContent.toString();
    }

    /**
     * Build String important
     * @param inputString given String
     * @return formatted String
     */
    private static String buildStringImportant(final String inputString) {
        return String.format(STRING_IMPORTANT, inputString);
    }

    /**
     * Building Time-Zone select
     * @return Content
     */
    public static String buildTimeZoneSelect(final String inTimeZone) {
        final SequencedMap<String, String> sortedTimeZones = ZoneDataServiceClass.loadSupportedTimeZones();
        final Properties selectProps = new Properties();
        selectProps.put("Name", "TZ");
        selectProps.put("Id", "TZ");
        selectProps.put("Default", inTimeZone);
        selectProps.put("Size", 1);
        return SelectInputSubClass.buildSelectInput(sortedTimeZones, selectProps);
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
            final String strLabel = objFeatures.getOrDefault("Label", "").toString()
                    + (objFeatures.getOrDefault(BasicStructuresClass.STR_MULTIPLE, "").toString().isEmpty() ? "" : "<sup>(multiple values possible)</sup>");
            final String tagLabelRaw = "<label for=\"%s\"%s>%s:</label>";
            final String strLabelStyle = objFeatures.getOrDefault("Label Style", "").toString().isEmpty() ? "" : "style=\"" + objFeatures.get("Label Style").toString() + "\"";
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
            if (!objFeatures.getOrDefault("Label", "").toString().isEmpty()) {
                outHtml.add(buildLabelTag(objFeatures));
            }
            manageAdditionalAttributesAndDefaults(objFeatures);
            outHtml.add(String.format("<select name=\"%s\" id=\"%s\"%s>", objFeatures.get("Name"), objFeatures.get("Id"), additionalAttrib));
            mapValues.forEach((strValue, strText) -> {
                String strSelected = "";
                if (!defaults.isEmpty()
                        && defaults.contains(strValue)) {
                    strSelected = " selected";
                }
                outHtml.add(String.format("<option value=\"%s\"%s>%s</option>", strValue, strSelected, strText));
            });
            outHtml.add("</select>");
            return String.join("", outHtml);
        }

        private static void manageAdditionalAttributesAndDefaults(final Properties objFeatures) {
            final String defaultValue = objFeatures.getOrDefault("Default", "").toString();
            String[] defaultVals = {defaultValue};
            if (!objFeatures.getOrDefault(BasicStructuresClass.STR_MULTIPLE, "").toString().isEmpty()) {
                if (!defaultValue.isEmpty()) {
                    defaultVals = defaultValue.split(",");
                }
                additionalAttrib = String.format(" multiple size=\"%s\"", objFeatures.get(BasicStructuresClass.STR_MULTIPLE));
            }
            if (!objFeatures.getOrDefault("Size", "").toString().isEmpty()) {
                additionalAttrib = String.format(" size=\"%s\"", objFeatures.get("Size"));
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
        private static final String CSS_TEXT_RIGHT = "text-align:right;";
        /** variable for Current Tab value */
        private static String currentTabValue;
        /** variable for HTML Table */
        private static final List<String> listTableLines = new ArrayList<>();
        /** Time Zone variable */
        private static final long LARGE_STRING = 20;
        /** variable for Remember Key */
        private static String rememberKey;
        /** variable for row counter */
        private static int rowCounter;
        /** variable for Table Header */
        private static String strTableHeader = "";
        /** Time Zone variable */
        private static String strTimeZone;
        /** variable for Counter usage */
        private static boolean useCounter;

        /**
         * Generate HTML from a Map of values
         * @param inList values stored as a list
         * @return String
         */
        public static String getListOfSequencedMapIntoHtmlTable(final List<SequencedMap<Object, Object>> inList, final Properties objFeatures) {
            if (strTimeZone == null) {
                setTimeZone(System.getProperty("user.timezone"));
            }
            listTableLines.clear();
            strTableHeader = "";
            rememberKey = getRememberKey(objFeatures);
            useCounter = !objFeatures.getOrDefault("Counter", "").toString().isEmpty();
            for (final SequencedMap<Object, Object> recordMap : inList) {
                processRecord(recordMap);
            }
            finish();
            return String.join("", listTableLines);
        }

        /**
         * final
         */
        private static void finish() {
            if (!strTableHeader.isEmpty()) {
                listTableLines.add("</tbody></table>");
                if (!rememberKey.isEmpty()) {
                    listTableLines.add(String.format("</div><!-- %s --></div><!-- tabStandard -->", currentTabValue));
                }
            }
        }

        /**
         * establishing the Key to Remember if relevant
         * @param objFeatures optional HTML Table features
         * @return String
         */
        private static String getRememberKey(final Properties objFeatures) {
            String strRememberKey = "";
            if (objFeatures.containsKey(BasicStructuresClass.STR_NEW_TAB)) {
                strRememberKey = objFeatures.get(BasicStructuresClass.STR_NEW_TAB).toString();
            }
            return strRememberKey;
        }

        /**
         * handle Tab switch
         * @param recordMap properties of the record to be transformed into HTML row
         */
        private static void handleTabSwitch(final SequencedMap<Object, Object> recordMap) {
            final Object valObj = recordMap.get(rememberKey);
            final String valueForTab = valObj == null ? "null" : valObj.toString();
            final String prev = currentTabValue == null ? "" : currentTabValue;
            if (!valueForTab.equalsIgnoreCase(prev)) {
                if (listTableLines.isEmpty()) {
                    // first tab: open tab container
                    listTableLines.add("<div id=\"tabStandard\" class=\"tabber\">");
                } else if (currentTabValue != null) {
                    // close previous tab's table
                    listTableLines.add(String.format("</tbody></table></div><!-- %s -->", currentTabValue));
                }
                // open new tab with header
                listTableLines.add(String.format("<div class=\"tabbertab\" title=\"%s\">%s", valueForTab, strTableHeader));
                currentTabValue = valueForTab;
                rowCounter = 0;
            }
        }

        /**
         * process each record
         * @param recordMap map with record content
         */
        private static void processRecord(final SequencedMap<Object, Object> recordMap) {
            HeaderSubSubClass.ensureHeaderExists(recordMap);
            if (rememberKey.isEmpty()) {
                HeaderSubSubClass.ensureHeaderAppended();
            } else {
                handleTabSwitch(recordMap);
            }
            if (useCounter) {
                rowCounter++;
                recordMap.put("#", String.valueOf(rowCounter));
            }
            listTableLines.add(RowSubSubClass.buildTableBodyRow(recordMap));
        }

        /**
         * Setter for strTimeZone
         * @param inTimeZone input time zone
         */
        public static void setTimeZone(final String inTimeZone) {
            strTimeZone = inTimeZone;
            TimingClass.LocalizationSubClass.setOutputTimeZone(inTimeZone);
        }

        /**
         * Rows logic
         */
        private static final class RowSubSubClass {

            /**
             * Table Body row logic
             * @param recordMap properties of the record to be transformed into HTML row
             * @return String
             */
            private static String buildTableBodyRow(final SequencedMap<Object, Object> recordMap) {
                final StringBuilder strTableRow = new StringBuilder(1000);
                strTableRow.append("<tr>");
                recordMap.forEach((strKey, objValue) -> {
                    if (!rememberKey.equalsIgnoreCase(strKey.toString())
                            && !BasicStructuresClass.STR_ROW_STYLE.equalsIgnoreCase(strKey.toString())) {
                        final StringBuilder cellStyle = new StringBuilder(100);
                        if (recordMap.containsKey(BasicStructuresClass.STR_ROW_STYLE)) {
                            cellStyle.append(recordMap.get(BasicStructuresClass.STR_ROW_STYLE).toString());
                        }
                        final Map<String, String> mapSmartLogic = manageCellStyleAndValue(objValue);
                        final String strValue = mapSmartLogic.get("value");
                        if (!mapSmartLogic.get(BasicStructuresClass.STR_STYLE).isEmpty()) {
                            cellStyle.append(mapSmartLogic.get(BasicStructuresClass.STR_STYLE));
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
             * right Style if Value is full Aging
             * @param inCellStyle input cell style
             * @param inValue input value
             * @return String new style if matches
             */
            private static String checkValueIfMatchesFullAging(final String inCellStyle, final String inValue) {
                String outCellStyle = inCellStyle;
                final boolean isFullAging = RegularExpressionsClass.ValidationSubClass.isStringActuallySomething(inValue, "fullAging");
                if (isFullAging) {
                    outCellStyle = CSS_TEXT_RIGHT + "white-space:nowrap;";
                }
                return outCellStyle;
            }

            /**
             * right Style if Value is byte or multiple of
             * @param inCellStyle input cell style
             * @param inValue input value
             * @return String new style if matches
             */
            private static String checkValueIfMatchesByteSizes(final String inCellStyle, final String inValue) {
                String outCellStyle = inCellStyle;
                final boolean isByteSize = RegularExpressionsClass.ValidationSubClass.isStringActuallySomething(inValue, "byteSize");
                if (isByteSize) {
                    outCellStyle = CSS_TEXT_RIGHT;
                }
                return outCellStyle;
            }

            /**
             * Manage Cell Style and Value
             * @param inValue input value
             * @return Map
             */
            private static Map<String, String> manageCellStyleAndValue(final Object inValue) {
                String cellStyle = "";
                String strValue = inValue.toString();
                if (BasicStructuresClass.STR_NULL.equalsIgnoreCase(strValue)) {
                    cellStyle = "color:LightGrey;font-style:italic;";
                    strValue = "&lt;NULL&gt;";
                } else if (strValue.isBlank()) {
                    cellStyle = "color:Grey;font-style:italic;";
                    strValue = "&lt;blank&gt;";
                } else if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyDecimal(strValue)) {
                    cellStyle = CSS_TEXT_RIGHT;
                    strValue = String.format(Locale.US, "%,.2f", new BigDecimal(strValue));
                } else if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyInteger(strValue)) {
                    cellStyle = CSS_TEXT_RIGHT;
                    strValue = String.format(Locale.US, "%,d", BasicStructuresClass.convertStringIntoInteger(strValue));
                } else if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyLong(strValue)) {
                    cellStyle = CSS_TEXT_RIGHT;
                    strValue = String.format(Locale.US, "%,d", BasicStructuresClass.convertStringIntoLong(strValue));
                } else if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyTimestampWithMilliseconds(strValue)) {
                    cellStyle = CSS_TEXT_RIGHT;
                    strValue = TimingClass.LocalizationSubClass.convertTimestampFriendly(strValue, TimingClass.DATE_TIME_MS, TimingClass.DATE_TIME_MS_ABRV);
                } else if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyTimestamp(strValue)) {
                    cellStyle = CSS_TEXT_RIGHT;
                    strValue = TimingClass.LocalizationSubClass.convertTimestampFriendly(strValue, TimingClass.DATE_TIME, TimingClass.DATE_TIME_ABRV);
                } else if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyDate(strValue)) {
                    cellStyle = CSS_TEXT_RIGHT;
                    strValue = TimingClass.LocalizationSubClass.formatDateFriendly(strValue, TimingClass.ISO_DATE, TimingClass.ISO_DATE_ABRV);
                } else if (strValue.length() >= LARGE_STRING) {
                    strValue = RegularExpressionsClass.replacePatternsWithTimeZones(strValue);
                }
                if (cellStyle.isBlank()) {
                    cellStyle = checkValueIfMatchesByteSizes(cellStyle, strValue); // check for disk size
                }
                if (cellStyle.isBlank()) {
                    cellStyle = checkValueIfMatchesFullAging(cellStyle, strValue); // check for full Aging
                }
                return Map.of(
                        BasicStructuresClass.STR_STYLE,
                        cellStyle,
                        "value",
                        strValue);
            }

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
            private static String buildTableHeader(final SequencedMap<Object, Object> recordMap) {
                final StringBuilder strBuilder = new StringBuilder(100);
                strBuilder.append("<table><thead>");
                recordMap.forEach((strKey, _) -> {
                    if (!rememberKey.equalsIgnoreCase(strKey.toString())
                            && !BasicStructuresClass.STR_ROW_STYLE.equalsIgnoreCase(strKey.toString())) {
                        strBuilder.append(String.format("<th>%s</th>", strKey));
                    }
                });
                if (useCounter) {
                    strBuilder.append("<th>#</th>");
                }
                strBuilder.append("</thead><tbody>");
                return strBuilder.toString();
            }

            /**
             * ensuring Table Header is appended
             */
            private static void ensureHeaderAppended() {
                if (listTableLines.isEmpty()) {
                    listTableLines.add(strTableHeader);
                    rowCounter = 0;
                }
            }

            /**
             * initiating Table Header
             * @param recordMap records to parse
             */
            private static void ensureHeaderExists(final SequencedMap<Object, Object> recordMap) {
                if (strTableHeader.isEmpty()) {
                    strTableHeader = buildTableHeader(recordMap);
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
