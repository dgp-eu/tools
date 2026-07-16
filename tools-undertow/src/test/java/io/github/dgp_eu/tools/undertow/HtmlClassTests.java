package io.github.dgp_eu.tools.undertow;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SequencedMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.ProjectClass;
import io.github.dgp_eu.tools.core.TimingClass;

/**
 * HtmlClass tests
 */
class HtmlClassTests {

    @Test
    @DisplayName("buildApplicationCopyright should be returned as String")
    void buildApplicationCopyright() {
        ProjectClass.setPomFile("/tools-core-pom.xml");
        final String appCopyright = HtmlClass.buildApplicationCopyright();
        assertTrue(appCopyright.contains("&copy; by "), "Application copyright should have Copyright symbol followed by single space and \"by\" word");
    }

    @Test
    @DisplayName("buildApplicationDetail should be returned as String")
    void buildApplicationDetail() {
        ProjectClass.setPomFile("/tools-core-pom.xml");
        final String appDetail = HtmlClass.buildApplicationDetail();
        assertTrue(appDetail.contains("&trade; v."), "Application detail should have TradeMark symbol followed by single space, small letter v and dot");
    }

    @Test
    @DisplayName("buildGeographicalCoordinatesFromTimeZone returns coordinates for known zone")
    void buildGeographicalCoordinatesKnownZoneReturnsCoordinates() {
        final String coords = HtmlClass.buildGeographicalCoordinatesFromTimeZone("Europe/London");
        assertAll("Coordinates for known zone",
                () -> assertNotNull(coords, "Coordinates should not be null"),
                () -> assertNotEquals("0,0", coords, "Known zone should not return 0,0"),
                () -> assertTrue(coords.contains(","), "Coordinates should contain comma-separated latitude and longitude")
        );
    }

    @Test
    @DisplayName("buildGeographicalCoordinatesFromTimeZone returns 0,0 for unknown zone")
    void buildGeographicalCoordinatesUnknownZoneReturnsZeroZero() {
        final String coords = HtmlClass.buildGeographicalCoordinatesFromTimeZone("Invalid/NonExistent_Zone");
        assertEquals("0,0", coords, "Unknown time zone should return default 0,0");
    }

    @Test
    @DisplayName("SelectInputSubClass.buildSelectInput produces select with selected option and correct attributes")
    void buildSelectInputGeneratesSelectWithSelectedOption() {
        final SequencedMap<String, String> mapValues = new LinkedHashMap<>();
        mapValues.put("tz1", "Timezone One");
        mapValues.put("tz2", "Timezone Two");
        final Properties props = new Properties();
        props.put("Label", "Time Zones");
        props.put("Name", "TZ");
        props.put("Id", "TZ");
        props.put("Default", "tz1");
        props.put("Size", "1");
        final String html = HtmlClass.SelectInputSubClass.buildSelectInput(mapValues, props);
        assertAll("Select HTML correctness",
                () -> assertTrue(html.contains("<label for=\"TZ"), "Label should exist point to actual select id"),
                () -> assertTrue(html.contains("<select"), "HTML should contain select tag"),
                () -> assertTrue(html.contains("id=\"TZ\""), "Select should contain provided id"),
                () -> assertTrue(html.contains("value=\"tz1\"") && html.contains(">Timezone One<"), "Option for tz1 should be present"),
                () -> assertTrue(html.contains("selected"), "Default option should be marked selected")
        );
    }

    @Test
    @DisplayName("HtmlClass.buildFileInfoBox produces div with time zones")
    void buildFileInfoBox() throws IOException {
        final Path tempFile = Files.createTempFile("fileops-test-file-", ".txt");
        try {
            final String myInfoBox = HtmlClass.buildFileInfoBox(tempFile);
            assertAll("Select HTML correctness",
                    () -> assertTrue(myInfoBox.contains("<div class=\"infoBox\""), "HTML should contain div tag with infoBox class"),
                    () -> assertTrue(myInfoBox.contains("bytes, last modified time-stamp on"), "Select contains final sequence text pieces")
            );
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("HtmlClass.buildMenuString produces select with time zones")
    void buildMenuString() {
        final SequencedMap<String, Map<String, String>> inMapMenu = Stream.of(
                Map.entry("home", Map.of(
                        BasicStructuresClass.STR_ICON, "fa-solid fa-house-user",
                        BasicStructuresClass.STR_MENU, "HomePage",
                        BasicStructuresClass.STR_TITLE, "HomePage"))
        ).collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, _) -> v1,
                        LinkedHashMap::new)  // Ensures it returns a SequencedMap
        );
        final String myMenu = HtmlClass.buildMenuString(inMapMenu);
        assertAll("Select HTML correctness",
                () -> assertTrue(myMenu.contains("<li>"), "HTML should contain li tag"),
                () -> assertTrue(myMenu.contains("<a href=\"?page=home\""), "Select should contain provided id")
        );
    }

    @Test
    @DisplayName("HtmlClass.buildTimeZoneSelect produces select with time zones")
    void buildTimeZoneSelect() {
        final String strHtml = HtmlClass.buildTimeZoneSelect("Europe/Bucharest");
        assertAll("Select HTML correctness from " + strHtml,
                () -> assertTrue(strHtml.contains("<select"), "HTML should contain select tag"),
                () -> assertTrue(strHtml.contains("id=\"TZ\""), "Select should contain provided id")
        );
    }

    @Test
    @DisplayName("TableSubClass.getListOfSequencedMapIntoHtmlTable generates table rows, headers and tabs when New Tab feature is used")
    void testGetListOfSequencedMapIntoHtmlTableProducesTableAndTabs() {
        final SequencedMap<Object, Object> rec1 = new LinkedHashMap<>();
        TimingClass.LocalizationSubClass.setOutputTimeZone(ZoneId.systemDefault().toString());
        rec1.put("Category", "A");
        rec1.put("Name", "Item1");
        rec1.put("Quantity", "10");
        rec1.put("Price", "1.99");
        rec1.put("Overall", "22147483649");
        rec1.put("When", "2026-01-01");
        rec1.put("Due", "2026-01-29 23:12:05");
        rec1.put("Comments", "");
        rec1.put("Obs.", "-");
        final SequencedMap<Object, Object> rec2 = new LinkedHashMap<>();
        rec2.put("Category", "B");
        rec2.put("Name", "Item2");
        rec2.put("Quantity", "15");
        rec2.put("Price", "0.9");
        rec2.put("Overall", "");
        rec2.put("When", "2026-06-01");
        rec2.put("Due", "NULL");
        rec2.put("Comments", "2026-06-26 23:59:59.555");
        rec2.put("Obs.", "Lorem ipsum dolor sit amet consectetur adipiscing elit quisque faucibus ex sapien vitae pellentesque sem placerat in id cursus mi pretium tellus duis convallis tempus leo eu aenean sed diam urna tempor pulvinar vivamus fringilla lacus nec metus bibendum egestas iaculis massa nisl malesuada lacinia integer nunc posuere ut hendrerit semper vel class aptent taciti sociosqu ad litora torquent per conubia nostra inceptos himenaeos orci varius natoque penatibus et magnis dis parturient montes nascetur ridiculus mus donec rhoncus eros lobortis nulla molestie mattis scelerisque maximus eget fermentum odio phasellus non purus est efficitur laoreet mauris pharetra vestibulum fusce dictum risus.");
        final List<SequencedMap<Object, Object>> records = List.of(rec1, rec2);
        final Properties features = new Properties();
        features.put(BasicStructuresClass.STR_NEW_TAB, "Category");
        features.put("Counter", "1");
        final String html = HtmlClass.TableSubClass.getListOfSequencedMapIntoHtmlTable(records, features);
        assertAll("HTML table with tabs and counter",
                () -> assertTrue(html.contains("<table"), "Output should contain table markup"),
                () -> assertTrue(html.contains("<tr>"), "Output should contain table rows"),
                () -> assertTrue(html.contains("tabber"), "Output should include tab container when New Tab feature is used"),
                () -> assertTrue(html.contains("<th>#</th>"), "Counter column header should be present when Counter feature is used"),
                () -> assertTrue(html.contains("</tbody></table>"), "Table should be properly closed")
        );
    }

    /**
     * Constructor
     */
    HtmlClassTests() {
        // intentionally blank
    }

}
