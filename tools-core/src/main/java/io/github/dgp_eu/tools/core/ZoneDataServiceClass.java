/**
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SequencedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Time Zones and associated coordinates handler
 */
public final class ZoneDataServiceClass {
    /**
     * Number of elements where coordinates are present
     */
    private static final int LINE_W_COORDINATE = 3;
    /**
     * Time Zone file
     */
    private static final String TZ_FILE = "/time_zones/zone1970.tab";
    /**
     * Supported IANA time-zone identifiers
     */
    private static final List<String> SUPPORTED_TZ = new CopyOnWriteArrayList<>(List.of("America/Los_Angeles", "America/Phoenix", "America/Denver", "America/Chicago", "America/New_York", "Europe/Dublin", "Europe/London", "Europe/Prague", "Europe/Berlin", "Europe/Bucharest", "Asia/Kolkata", "Asia/Shanghai", "Asia/Tokyo", "Australia/Melbourne"));
    /**
     * Cached zones
     */
    private static final Map<String, ZoneInfoRecord> CACHE = new ConcurrentHashMap<>();
    /** Record for ZoneInfo */
    /* default */ public record ZoneInfoRecord(
        String zoneId,
        double latitude,
        double longitude,
        List<String> countryCodes,
        List<String> countryNames,
        String friendlyOffset) {}

    static {
        loadIanaZones();
    }

    /**
     * IANA zone logic
     */
    private static void loadIanaZones() {
        try (InputStream inputStream = ZoneDataServiceClass.class.getResourceAsStream(TZ_FILE)) {
            assert inputStream != null;
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader bReader = new BufferedReader(inputStreamReader)) {
                bReader.lines()
                    .filter(line -> !line.startsWith("#") && !line.isBlank())
                    .forEach(line -> {
                        final String[] parts = line.split("\t");
                        if (parts.length >= LINE_W_COORDINATE) {
                            processLine(parts[0], parts[1], parts[2]);
                        }
                    });
            }
        } catch (IOException ei) {
            final Path ptPrjProps = Path.of(TZ_FILE);
            final String strFeedback = String.format(FileOperationsClass.FILE_FIND_ERR, ptPrjProps.getParent(), ptPrjProps.getFileName());
            LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
        }
    }

    /**
     * Populates a time-zone list sorted in chronological order
     * @return SequencedMap
     */
    public static SequencedMap<String, String> loadSupportedTimeZones() {
        final Collection<ZoneInfoRecord> allTimeZones = getAll();
        // ensure current user time-zone is also populated
        final String crtUserTimeZone = System.getProperty("user.timezone");
        if (crtUserTimeZone != null
                && !SUPPORTED_TZ.contains(crtUserTimeZone)
                && allTimeZones.stream().anyMatch(z -> z.zoneId().equals(crtUserTimeZone))) {
            SUPPORTED_TZ.add(crtUserTimeZone);
        }
        final Map<String, String> mapBeforeUtc = new ConcurrentHashMap<>();
        final Map<String, String> mapAfterUtc = new ConcurrentHashMap<>();
        for (final String crtTimeZone : SUPPORTED_TZ) {
            final String friendlyTimeZone = TimingClass.getFriendlyOffset(crtTimeZone);
            if (friendlyTimeZone.startsWith("UTC-")) {
                mapBeforeUtc.put(crtTimeZone, friendlyTimeZone + " " + crtTimeZone);
            } else {
                mapAfterUtc.put(crtTimeZone, friendlyTimeZone + " " + crtTimeZone);
            }
        }
        // building final TimeZone list
        final SequencedMap<String, String> sortedTimeZones = mapBeforeUtc.entrySet().stream()
                .sorted(Map.Entry.<String, String>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, _) -> oldValue,
                        LinkedHashMap::new // preserve sorted order
                ));
        final SequencedMap<String, String> sortedAfterUtc = mapAfterUtc.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, _) -> oldValue,
                        LinkedHashMap::new // preserve sorted order
                ));
        sortedTimeZones.putAll(sortedAfterUtc);
        return sortedTimeZones;
    }

    /**
     * Coordinates parser
     * @param countries countries list as single string separated by comma
     * @param coords coordinates raw
     * @param zoneId IANA zone identifier
     */
    private static void processLine(final String countries, final String coords, final String zoneId) {
        // Parse Countries (Java 19 Locale.of)
        final List<String> codes = Arrays.asList(countries.split(","));
        final List<String> names = codes.stream()
                .map(code -> Locale.of("", code).getDisplayCountry(Locale.ENGLISH))
                .toList();
        // 2. Parse Coordinates (ISO 6709)
        int splitIdx = coords.indexOf('-', 1);
        if (splitIdx == -1) {
            splitIdx = coords.indexOf('+', 1);
        }
        final double lat = RegularExpressionsClass.dmsToDecimal(coords.substring(0, splitIdx), false);
        final double lon = RegularExpressionsClass.dmsToDecimal(coords.substring(splitIdx), true);
        CACHE.put(zoneId, new ZoneInfoRecord(zoneId, lat, lon, codes, names, ""));
    }

    /**
     * capture Zone Info with current offset
     * @param inRecord original Zone Info record
     * @return UTC system Zone Info record
     */
    private static ZoneInfoRecord withCurrentOffset(final ZoneInfoRecord inRecord) {
        final String strOffset = "UTC" + ZonedDateTime.now(ZoneId.of(inRecord.zoneId())).getOffset().getId();
        return new ZoneInfoRecord(
                inRecord.zoneId(),
                inRecord.latitude(),
                inRecord.longitude(),
                inRecord.countryCodes(),
                inRecord.countryNames(),
                strOffset);
    }

    /**
     * Getter for ZoneInfo
     * @param zoneId string with IANA location
     * @return ZoneInfo
     */
    public static ZoneInfoRecord get(final String zoneId) {
        final ZoneInfoRecord ziRecord = CACHE.get(zoneId);
        return ziRecord == null ? null : withCurrentOffset(ziRecord);
    }

    /**
     * Getter for ZoneInfo
     * @return Collection of ZoneInfo
     */
    public static Collection<ZoneInfoRecord> getAll() {
        return CACHE.values().stream()
                .map(ZoneDataServiceClass::withCurrentOffset)
                .toList();
    }

    /**
     * Constructor
     */
    private ZoneDataServiceClass() {
        // intentionally blank
    }

}
