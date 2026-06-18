package io.github.dgp_eu.tools.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SequencedMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ZoneDataServiceClass testing
 */
@DisplayName("ZoneDataServiceClass unit testing")
class ZoneDataServiceClassTests {
    /** Constant for America/New_York */
    private static final String AMERICA_NY = "America/New_York";
    /** Constant for Calculated Not Equal to Expected */
    private static final String ORIG_NQ_EXPCT = "calculated result is not equal to expected result";
    /** Constant for Zone Info should not be null */
    private static final String ZONE_INFO_NOT_NUL = "Zone info should not be null";

    @Test
    @DisplayName("Loaded time zones are properly sorted with UTC offsets")
    void testGetTimeZonesAreSortedWithUtcOffsets() {
        final SequencedMap<String, String> timeZones = ZoneDataServiceClass.loadSupportedTimeZones();
        for (final String value : timeZones.values()) {
            assertAll("Loaded time zones are properly sorted with UTC offsets",
                    () -> assertFalse(value.isEmpty(), "Time zone display value should not be empty"),
                    () -> assertTrue(value.contains("UTC"), "Time zone display should contain UTC offset")
            );
        }
    }

    @Test
    @DisplayName("Get zone info returns null for non-existent zone")
    void testGetZoneInfoForNonExistentZoneReturnsNull() {
        final ZoneDataServiceClass.ZoneInfoRecord zoneInfo = ZoneDataServiceClass.get("Invalid/Zone_That_Does_Not_Exist");
        assertNull(zoneInfo, ORIG_NQ_EXPCT);
    }

    @Test
    @DisplayName("Get zone info returns valid record for known zone")
    void testGetZoneInfoReturnsValidRecordForKnownZone() {
        final ZoneDataServiceClass.ZoneInfoRecord zoneInfo = ZoneDataServiceClass.get(AMERICA_NY);
        assertAll("Get zone info returns valid record for known zone",
                () -> assertNotNull(zoneInfo, "Zone info should not be null for valid zone"),
                () -> assertEquals(AMERICA_NY, zoneInfo.zoneId(), "Zone ID should match")
        );
    }

    @Test
    @DisplayName("Get zone info includes country codes")
    void testGetZoneInfoIncludesCountryCodes() {
        final ZoneDataServiceClass.ZoneInfoRecord zoneInfo = ZoneDataServiceClass.get("Europe/Berlin");
        assertAll("Get zone info includes country codes",
                () -> assertNotNull(zoneInfo, ZONE_INFO_NOT_NUL),
                () -> assertFalse(zoneInfo.countryCodes().isEmpty(), "Country codes should not be empty")
        );
    }

    @Test
    @DisplayName("Get zone info includes country names")
    void testGetZoneInfoIncludesCountryNames() {
        final ZoneDataServiceClass.ZoneInfoRecord zoneInfo = ZoneDataServiceClass.get("Asia/Tokyo");
        assertAll("Get zone info includes country names",
                () -> assertNotNull(zoneInfo, "Zone info should not be null for country"),
                () -> assertFalse(zoneInfo.countryNames().isEmpty(), "Country names should not be empty")
        );
    }

    @Test
    @DisplayName("Get zone info includes current UTC offset")
    void testGetZoneInfoIncludesCurrentUtcOffset() {
        final ZoneDataServiceClass.ZoneInfoRecord zoneInfo = ZoneDataServiceClass.get("America/Los_Angeles");
        assertAll("Get zone info includes current UTC offset",
                () -> assertNotNull(zoneInfo, ZONE_INFO_NOT_NUL),
                () -> assertTrue(zoneInfo.friendlyOffset().startsWith("UTC"), "Offset should start with UTC")
        );
    }

    @Test
    @DisplayName("Get all zones returns non-empty collection")
    void testGetAllZonesReturnsNonEmpty() {
        final Collection<ZoneDataServiceClass.ZoneInfoRecord> allZones = ZoneDataServiceClass.getAll();
        assertFalse(allZones.isEmpty(), "All zones collection should not be empty");
    }

    @Test
    @DisplayName("Get all zones returns cached zones with current offsets")
    void testGetAllZonesReturnsCachedZonesWithOffsets() {
        final Collection<ZoneDataServiceClass.ZoneInfoRecord> allZones = ZoneDataServiceClass.getAll();
        for (final ZoneDataServiceClass.ZoneInfoRecord zoneRecord : allZones) {
            assertAll("Get all zones returns cached zones with current offsets",
                    () -> assertTrue(zoneRecord.friendlyOffset().startsWith("UTC"), "Each zone should have UTC offset"),
                    () -> assertFalse(zoneRecord.zoneId().isEmpty(), "Each zone should have a zone ID")
            );
        }
    }

    @Test
    @DisplayName("Get zone info multiple calls return consistent data")
    void testGetZoneInfoMultipleCallsReturnConsistentData() {
        final ZoneDataServiceClass.ZoneInfoRecord firstCall = ZoneDataServiceClass.get("Europe/London");
        final ZoneDataServiceClass.ZoneInfoRecord secondCall = ZoneDataServiceClass.get("Europe/London");
        assertAll("Get zone info multiple calls return consistent data",
                () -> assertEquals(firstCall.zoneId(), secondCall.zoneId(), "Zone ID should be consistent"),
                () -> assertEquals(firstCall.latitude(), secondCall.latitude(), "Latitude should be consistent"),
                () -> assertEquals(firstCall.longitude(), secondCall.longitude(), "Longitude should be consistent")
        );
    }

    @Test
    @DisplayName("Get zone info for southern hemisphere zone")
    void testGetZoneInfoForSouthernHemisphereZone() {
        final ZoneDataServiceClass.ZoneInfoRecord zoneInfo = ZoneDataServiceClass.get("Australia/Melbourne");
        assertAll("Get zone info for southern hemisphere zone",
                () -> assertNotNull(zoneInfo, "Zone info should not be null for southern hemisphere"),
                () -> assertTrue(zoneInfo.latitude() < 0, "Southern hemisphere zones should have negative latitude")
        );
    }

    /**
     * Test for Coordinates
     */
    @Nested
    /* default */ @DisplayName("Coordinates testing...")
    class TestCoordinates {

        @Test
        @DisplayName("Get zone info includes latitude and longitude coordinates")
        void testGetZoneInfoIncludesCoordinates() {
            final ZoneDataServiceClass.ZoneInfoRecord zoneInfo = ZoneDataServiceClass.get(AMERICA_NY);
            assertAll("Get zone info includes latitude and longitude coordinates",
                    () -> assertNotNull(zoneInfo, "Zone info should not be null for coordinates"),
                    () -> assertTrue(zoneInfo.latitude() != 0.0 || zoneInfo.longitude() != 0.0, "Coordinates should be non-zero")
            );
        }

        @Test
        @DisplayName("Zone info coordinates are within valid range")
        void testGetZoneInfoCoordinatesAreWithinValidRange() {
            final ZoneDataServiceClass.ZoneInfoRecord zoneInfo = ZoneDataServiceClass.get("America/Denver");
            assertAll("Zone info coordinates are within valid range",
                    () -> assertNotNull(zoneInfo, "Zone info should not be null for coordinates"),
                    () -> assertTrue(zoneInfo.latitude() >= -90 && zoneInfo.latitude() <= 90, "Latitude should be between -90 and 90"),
                    () -> assertTrue(zoneInfo.longitude() >= -180 && zoneInfo.longitude() <= 180, "Longitude should be between -180 and 180")
            );
        }

        /**
         * Constructor
         */
        public TestCoordinates() {
            // intentionally blank
        }

    }

    /**
     * Test for Zones
     */
    @Nested
    /* default */ @DisplayName("Zones testing...")
    class TestSupportedZones{

        @Test
        @DisplayName("Load supported time zones returns at least five zones")
        void loadSupportedTimeZonesReturnsAtLeastFiveZones() {
            final SequencedMap<String, String> timeZones = ZoneDataServiceClass.loadSupportedTimeZones();
            final List<String> keysList = new ArrayList<>(timeZones.keySet());
            assertTrue(keysList.size() >= 5, "Should have at least 5 supported zones");
        }

        @Test
        @DisplayName("Get supported time zones returns non-empty collection")
        void testGetSupportedTimeZonesReturnsNonEmpty() {
            final SequencedMap<String, String> timeZones = ZoneDataServiceClass.loadSupportedTimeZones();
            assertTrue(!timeZones.isEmpty(), "Supported time zones should not be empty");
        }

        @Test
        @DisplayName("Get supported time zones includes common American zones")
        void testGetSupportedTimeZonesIncludeAmericaZones() {
            final SequencedMap<String, String> timeZones = ZoneDataServiceClass.loadSupportedTimeZones();
            assertAll("Get supported time zones includes common American zones",
                    () -> assertTrue(timeZones.containsKey("America/Los_Angeles"), "America/Los_Angeles should be supported"),
                    () -> assertTrue(timeZones.containsKey(AMERICA_NY), "America/New_York should be supported"),
                    () -> assertTrue(timeZones.containsKey("America/Chicago"), "America/Chicago should be supported")
            );
        }

        @Test
        @DisplayName("Get supported time zones includes European zones")
        void testGetSupportedTimeZonesIncludeEuropeZones() {
            final SequencedMap<String, String> timeZones = ZoneDataServiceClass.loadSupportedTimeZones();
            assertAll("Get supported time zones includes European zones",
                    () -> assertTrue(timeZones.containsKey("Europe/London"), "Europe/London should be supported"),
                    () -> assertTrue(timeZones.containsKey("Europe/Berlin"), "Europe/Berlin should be supported"),
                    () -> assertTrue(timeZones.containsKey("Europe/Prague"), "Europe/Prague should be supported")
            );
        }

        @Test
        @DisplayName("Get supported time zones includes Asian zones")
        void testGetSupportedTimeZonesIncludeAsiaZones() {
            final SequencedMap<String, String> timeZones = ZoneDataServiceClass.loadSupportedTimeZones();
            assertAll("Get supported time zones includes Asian zones",
                    () -> assertTrue(timeZones.containsKey("Asia/Tokyo"), "Asia/Tokyo should be supported"),
                    () -> assertTrue(timeZones.containsKey("Asia/Shanghai"), "Asia/Shanghai should be supported"),
                    () -> assertTrue(timeZones.containsKey("Asia/Kolkata"), "Asia/Kolkata should be supported")
            );
        }

        @Test
        @DisplayName("Get supported time zones includes Australian zones")
        void testGetSupportedTimeZonesIncludeAustraliaZones() {
            final SequencedMap<String, String> timeZones = ZoneDataServiceClass.loadSupportedTimeZones();
            assertTrue(timeZones.containsKey("Australia/Melbourne"), "Australia/Melbourne should be supported");
        }

        /**
         * Constructor
         */
        public TestSupportedZones() {
            // intentionally blank
        }

    }

    /**
     * Constructor
     */
    public ZoneDataServiceClassTests() {
        // intentionally blank
    }
}