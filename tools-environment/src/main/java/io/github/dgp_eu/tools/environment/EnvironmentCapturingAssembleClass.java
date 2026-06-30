/*
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.environment;

import oshi.hardware.*;
import oshi.software.os.NetworkParams;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;
import oshi.util.PlatformEnum;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import io.github.dgp_eu.tools.core.BasicStructuresClass;
import io.github.dgp_eu.tools.core.JsonOperationsClass;
import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.core.ProjectClass;
import io.github.dgp_eu.tools.core.ShellingClass;

/**
 * Capturing current environment details
 */
public final class EnvironmentCapturingAssembleClass {

    /**
     * Constructor
     */
    private EnvironmentCapturingAssembleClass() {
        super();
    }

    /**
     * Environment details gathered
     * @return Map
     */
    private static Map<String, Object> gatherEnvironmentDetails() {
        final String strInsteadOfNull = "---";
        final String strComputer = getComputerName(strInsteadOfNull);
        final String username = getUserName(strInsteadOfNull);
        final String userAccount = ShellingClass.getCurrentUserAccount();
        return Map.of(
                "Computer", strComputer,
                "Country", System.getProperty("user.country", strInsteadOfNull),
                "Country.Format", System.getProperty("user.country.format", strInsteadOfNull),
                "Language", System.getProperty("user.language", strInsteadOfNull),
                "Language.Format", System.getProperty("user.language.format", strInsteadOfNull),
                "Home", System.getProperty("user.home", strInsteadOfNull).replace("\\", "\\\\"),
                "Name", System.getProperty("user.name", strInsteadOfNull),
                "Timezone", System.getProperty("user.timezone", strInsteadOfNull),
                "Username", username,
                "User Account", userAccount);
    }

    /**
     * Hardware details gathered
     * @return Map
     */
    private static Map<String, Object> gatherHardwareDetails() {
        return Map.of(
                "CPU", JsonOperationsClass.getMapIntoJsonString(HardwareSubClass.getDetailsAboutCentralProcessorUnit()),
                "GPU", JsonOperationsClass.getMapIntoJsonString(HardwareSubClass.getDetailsAboutGraphicCards()),
                "Mainboard", JsonOperationsClass.getMapIntoJsonString(HardwareSubClass.MotherboardAndSystemSubClass.getDetailsAboutMainboard()),
                "Monitor", JsonOperationsClass.getMapIntoJsonString(HardwareSubClass.getDetailsAboutMonitor()),
                "Network Interface", JsonOperationsClass.getMapIntoJsonString(HardwareSubClass.getDetailsAboutNetworkInterfaces()),
                "RAM", JsonOperationsClass.getMapIntoJsonString(HardwareSubClass.getDetailsAboutRandomAccessMemory()));
    }

    /**
     * Environment details gathered
     * @return Map
     */
    private static Map<String, Object> gatherJavaDetails() {
        return Map.of(
                "Release", System.getProperty("java.vendor.version"),
                "Runtime Name", System.getProperty("java.runtime.name"),
                "Runtime Version", System.getProperty("java.runtime.version"),
                BasicStructuresClass.STR_VENDOR, System.getProperty("java.vendor"),
                BasicStructuresClass.STR_VERSION, System.getProperty("java.version"),
                "Version Date", System.getProperty("java.version.date"),
                "VM Name", System.getProperty("java.vm.name"),
                "VM Version", System.getProperty("java.vm.version"),
                "VM Specification Name", System.getProperty("java.vm.specification.name"),
                "VM Specification Vendor", System.getProperty("java.vm.specification.vendor"));
    }

    /**
     * Software details gathered
     * @return Map
     */
    private static Map<String, Object> gatherSoftwareDetails() {
        return Map.of(
                "Java", JsonOperationsClass.getMapIntoJsonString(gatherJavaDetails()),
                "OS", JsonOperationsClass.getMapIntoJsonString(HardwareSubClass.getDetailsAboutOperatingSystem()),
                "Network", JsonOperationsClass.getMapIntoJsonString(HardwareSubClass.getDetailsAboutNetwork()),
                "Storage", JsonOperationsClass.getMapIntoJsonString(OshiUsageClass.getDetailsAboutAvailableStoragePartitions()));
    }

    /**
     * Capturing computer name
     * @param strInsteadOfNull alternative text if not found
     * @return String with computer name
     */
    private static String getComputerName(final String strInsteadOfNull) {
        String strComputer = System.getenv("COMPUTERNAME");
        if (strComputer == null) {
            strComputer = System.getenv("HOSTNAME");
        }
        if (strComputer == null) {
            strComputer = strInsteadOfNull;
        }
        return strComputer;
    }

    /**
     * Capturing user name
     * @param strInsteadOfNull alternative text if not found
     * @return String with user name
     */
    private static String getUserName(final String strInsteadOfNull) {
        String username = System.getenv("USERNAME");
        if (username == null) {
            username = System.getProperty("user.name", strInsteadOfNull);
        }
        return username;
    }

    /**
     * Capturing current Environment details
     * 
     * @return String
     */
    public static String packageCurrentEnvironmentDetailsIntoJson() {
        final StringBuilder strJsonString = new StringBuilder(1000).append('{');
        final String strFeedback = "Capturing information...";
        LogExposureClass.LOGGER.info(strFeedback);
        final String strHardware = JsonOperationsClass.getMapIntoJsonString(gatherHardwareDetails());
        if (strHardware != null) {
            strJsonString.append("\"Hardware\":").append(strHardware);
        }
        final String strFeedbackH = "I just captured Hardware information...";
        LogExposureClass.LOGGER.debug(strFeedbackH);
        final String strSoftware = JsonOperationsClass.getMapIntoJsonString(gatherSoftwareDetails());
        if (strSoftware != null) {
            strJsonString.append(",\"Software\":").append(strSoftware);
        }
        final String strFeedbackS = "I just captured Software information...";
        LogExposureClass.LOGGER.debug(strFeedbackS);
        final String strAppDetails = ProjectClass.ApplicationSubClass.getApplicationDetails();
        if (strAppDetails != null) {
            strJsonString.append(',').append(strAppDetails);
        }
        final String strEnvironment = JsonOperationsClass.getMapIntoJsonString(gatherEnvironmentDetails());
        final String strFeedbackEnv = "I just captured Environment information...";
        LogExposureClass.LOGGER.debug(strFeedbackEnv);
        if (strEnvironment != null) {
            strJsonString.append(",\"Environment\":").append(strEnvironment);
        }
        return BasicStructuresClass.StringCleaningSubClass.ensureEscapingForValidJson(strJsonString.append('}').toString());
    }

    /**
     * Capturing current Environment details
     * 
     * @return String
     */
    public static List<Properties> packageCurrentEnvironmentDetailsIntoListOfProperties() {
        final List<Properties> resultReleases = new ArrayList<>();
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Environment", gatherEnvironmentDetails()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Hardware - CPU", HardwareSubClass.getDetailsAboutCentralProcessorUnit()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Hardware - GPU", HardwareSubClass.getDetailsAboutGraphicCards()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Hardware - Mainboard", HardwareSubClass.MotherboardAndSystemSubClass.getDetailsAboutMainboard()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Hardware - Monitors", HardwareSubClass.getDetailsAboutMonitor()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Hardware - Network Interfaces", HardwareSubClass.getDetailsAboutNetworkInterfaces()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Hardware - RAM", HardwareSubClass.getDetailsAboutRandomAccessMemory()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Software - Java", gatherJavaDetails()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Software - OS", HardwareSubClass.getDetailsAboutOperatingSystem()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Software - Network", HardwareSubClass.getDetailsAboutNetwork()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Software - Storage", OshiUsageClass.getDetailsAboutAvailableStoragePartitions()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Application", ProjectClass.ApplicationSubClass.getApplicationDetailsIntoMap()));
        return resultReleases;
    }

    /**
     * Hardware class
     */
    public static final class HardwareSubClass {

        /**
         * Constructor
         */
        private HardwareSubClass() {
            // intentionally blank
        }

        /**
         * Display details
         *
         * @param crtDisplay current Display object
         * @return String
         */
        private static Map<String, Object> digestSingleDisplayDetails(final Display crtDisplay) {
            final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>();
            final String[] arrayDetails = crtDisplay.toString().replaceAll("[^a-zA-Z0-9\\s]", "").split("\n");
            for (final String crtLine : arrayDetails) {
                final String strSlimLine = crtLine.trim();
                if (strSlimLine.endsWith(" in") && strSlimLine.contains(" cm ")) {
                    final int intCmPos = strSlimLine.indexOf(" cm ");
                    arrayAttributes.put(BasicStructuresClass.STR_PHYSC_DIM + " [in]", strSlimLine.substring(0, intCmPos));
                    final int intInPos = strSlimLine.indexOf(" in");
                    arrayAttributes.put(BasicStructuresClass.STR_PHYSC_DIM + " [cm]", strSlimLine.substring(intCmPos + 4, intInPos));
                }
                if (strSlimLine.startsWith(BasicStructuresClass.STR_MONITOR_NAME)) {
                    arrayAttributes.put(BasicStructuresClass.STR_MONITOR_NAME, strSlimLine.replace(BasicStructuresClass.STR_MONITOR_NAME + " ", ""));
                }
                if (strSlimLine.startsWith(BasicStructuresClass.STR_PRFRD_TM_CLCK)) {
                    final int intClockLen = BasicStructuresClass.STR_PRFRD_TM_CLCK.length();
                    final int intPixelPos = strSlimLine.indexOf(BasicStructuresClass.STR_ACTV_PXLS);
                    arrayAttributes.put(BasicStructuresClass.STR_PRFRD_TM_CLCK, strSlimLine.substring(intClockLen, intPixelPos).trim());
                    arrayAttributes.put(BasicStructuresClass.STR_ACTV_PXLS, strSlimLine.substring(intPixelPos)
                            .replace(BasicStructuresClass.STR_ACTV_PXLS + " ", "").trim());
                }
                if (strSlimLine.startsWith(BasicStructuresClass.STR_RANGE_LMTS)) {
                    arrayAttributes.put(BasicStructuresClass.STR_RANGE_LMTS, strSlimLine.replace(BasicStructuresClass.STR_RANGE_LMTS + " ", ""));
                }
                if (strSlimLine.startsWith(BasicStructuresClass.STR_SRL_NUM)) {
                    arrayAttributes.put(BasicStructuresClass.STR_SRL_NUM, strSlimLine.replace(BasicStructuresClass.STR_SRL_NUM + " ", ""));
                }
            }
            return arrayAttributes;
        }

        /**
         * Environment details gathered
         * @return Map
         */
        public static Map<String, Object> getDetailsAboutCentralProcessorUnit() {
            final CentralProcessor processor = OshiUsageClass.HardwareSubClass.getOshiProcessor();
            final CentralProcessor.ProcessorIdentifier procIdentif = OshiUsageClass.HardwareSubClass.getOshiProcessorIdentifier();
            final List<String> featureFlags = processor.getFeatureFlags().stream()
                    .sorted()
                    .toList();
            return Map.of(
                    "CPU Identifier", procIdentif.getIdentifier(),
                    "Family", procIdentif.getFamily(),
                    "Feature Flags", featureFlags.toString().replace("[", "[\"").replace(", ", "\", \"").replace("]", "\"]"),
                    "Logical Processors", processor.getLogicalProcessorCount(),
                    "Maximum Frequency", FormatUtil.formatHertz(processor.getMaxFreq()),
                    BasicStructuresClass.STR_MODEL, procIdentif.getModel(),
                    BasicStructuresClass.STR_NAME, procIdentif.getName(),
                    "Processor ID", procIdentif.getProcessorID(),
                    "Physical Processors", processor.getPhysicalProcessorCount(),
                    BasicStructuresClass.STR_VENDOR, procIdentif.getVendor());
        }

        /**
         * GPU info
         *
         * @return Map
         */
        public static Map<String, Object> getDetailsAboutGraphicCards() {
            final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>();
            final List<GraphicsCard> graphicCards = OshiUsageClass.HardwareSubClass.getOshiGraphicsCards();
            for (final GraphicsCard  graphicCard : graphicCards) {
                final String strIdentifier = "Video Card ID#" + BasicStructuresClass.StringTransformationSubClass.computeStringSignature(graphicCard.getName()) + " ";
                arrayAttributes.putAll(Map.of(
                        strIdentifier + BasicStructuresClass.STR_NAME, graphicCard.getName(),
                        strIdentifier + BasicStructuresClass.STR_VENDOR, graphicCard.getVendor(),
                        strIdentifier + "VRAM", FormatUtil.formatBytes(graphicCard.getVRam()),
                        strIdentifier + "Driver Version", graphicCard.getVersionInfo()
                ));
            }
            return arrayAttributes;
        }

        /**
         * Monitors info as Map
         *
         * @return Map
         */
        public static Map<String, Object> getDetailsAboutMonitor() {
            final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>();
            final List<Display> displays = OshiUsageClass.HardwareSubClass.getOshiMonitor();
            for (final Display crtDisplay : displays) {// The EDID is the "fingerprint" of the monitor hardware
                final byte[] edid = crtDisplay.getEdid();
                final String uniqueId = "Monitor #" + BasicStructuresClass.StringTransformationSubClass.computeStringSignature(Base64.getEncoder().encodeToString(edid));
                final Map<String, Object> crtMonitor = digestSingleDisplayDetails(crtDisplay);
                crtMonitor.forEach((strKey, strValue) -> arrayAttributes.put(uniqueId + " " + strKey, strValue));
            }
            return arrayAttributes;
        }

        /**
         * Network details gathered
         * @return Map
         */
        public static Map<String, Object> getDetailsAboutNetwork() {
            final NetworkParams networkParams = OshiUsageClass.SoftwareSubClass.getOshiNetworkParameters();
            return Map.of(
                    //"DNS Servers", String.join(", ", networkParams.getDnsServers()),
                    "Domain Name", networkParams.getDomainName(),
                    "Host Name", networkParams.getHostName(),
                    "IPv4 Gateway", networkParams.getIpv4DefaultGateway(),
                    "IPv6 Gateway", networkParams.getIpv6DefaultGateway());
        }

        /**
         * Sensors Information
         *
         * @return Map
         */
        public static Map<String, Object> getDetailsAboutNetworkInterfaces() {
            final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>();
            final List<NetworkIF> networkIFs = OshiUsageClass.HardwareSubClass.getOshiNetworkInterfaces();
            for (final NetworkIF net : networkIFs) {
                net.updateAttributes(); // Refresh interface stats
                final String strIdentifier = "Memory MAC#" + net.getMacaddr() + " ";
                arrayAttributes.putAll(Map.of(
                        strIdentifier + BasicStructuresClass.STR_NAME, net.getName(),
                        strIdentifier + "Display Name", net.getDisplayName(),
                        strIdentifier + "IPv4", String.join(", ", net.getIPv4addr()),
                        strIdentifier + "IPv6", String.join(", ", net.getIPv6addr()),
                        strIdentifier + "MTU", net.getMTU(),
                        strIdentifier + "NDIS Physical Medium Type", OshiUsageClass.getNetworkPhysicalMediumType(net.getNdisPhysicalMediumType()),
                        strIdentifier + "Status", net.getIfOperStatus(),
                        strIdentifier + "Speed", FormatUtil.formatBytes(net.getSpeed())));
            }
            return arrayAttributes;
        }

        /**
         * Operating System details gathered
         * @return Map
         */
        public static Map<String, Object> getDetailsAboutOperatingSystem() {
            final OperatingSystem.OSVersionInfo version = OshiUsageClass.SoftwareSubClass.getOshiVersionInfo();
            return Map.of(
                    "Architecture", System.getProperty("os.arch"),
                    "Build", version.getBuildNumber(),
                    "Code", version.getCodeName(),
                    "Family", OshiUsageClass.SoftwareSubClass.getOshiFamily(),
                    BasicStructuresClass.STR_MANUFACTURER, OshiUsageClass.SoftwareSubClass.getOshiManufacturer(),
                    BasicStructuresClass.STR_NAME, System.getProperty("os.name"),
                    "Platform", PlatformEnum.getCurrentPlatform().toString(),
                    BasicStructuresClass.STR_VERSION, version.getVersion());
        }

        /**
         * Capturing RAM information
         *
         * @return Map
         */
        public static Map<String, Object> getDetailsAboutRandomAccessMemory() {
            final GlobalMemory globalMemory = OshiUsageClass.HardwareSubClass.getOshiMemory();
            final VirtualMemory virtualMemory = OshiUsageClass.HardwareSubClass.getOshiVirtualMemory();
            final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>(Map.of(
                    "Available", FormatUtil.formatBytes(globalMemory.getAvailable()),
                    "Page Size", FormatUtil.formatBytes(globalMemory.getPageSize()),
                    "Total", FormatUtil.formatBytes(globalMemory.getTotal()),
                    "Virtual Memory Swap In Use", FormatUtil.formatBytes(virtualMemory.getVirtualInUse()),
                    "Virtual Memory Swap Used", FormatUtil.formatBytes(virtualMemory.getSwapUsed()),
                    "Virtual Memory Swap Total", FormatUtil.formatBytes(virtualMemory.getSwapTotal())));
            final List<PhysicalMemory> physicalMemories = globalMemory.getPhysicalMemory();
            for (final PhysicalMemory physicalMemory : physicalMemories) {
                final String strIdentifier = "Bank SN#" + physicalMemory.getSerialNumber() + " ";
                arrayAttributes.putAll(Map.of(
                        strIdentifier + "Bank/Slot Label", physicalMemory.getBankLabel(),
                        strIdentifier + "Capacity", FormatUtil.formatBytes(physicalMemory.getCapacity()),
                        strIdentifier + "Clock Speed", FormatUtil.formatHertz(physicalMemory.getClockSpeed()),
                        strIdentifier + BasicStructuresClass.STR_MANUFACTURER, physicalMemory.getManufacturer(),
                        strIdentifier + "Type", physicalMemory.getMemoryType(),
                        strIdentifier + "Part Number", physicalMemory.getPartNumber().trim()));
            }
            return arrayAttributes;
        }

        /**
         * Hardware class
         */
        public static final class MotherboardAndSystemSubClass {

            /**
             * Constructor
             */
            private MotherboardAndSystemSubClass() {
                // intentionally blank
            }

            /**
             * capture Computer System parameters into Map
             * @return Map
             */
            private static Map<String, Object> getDetailsAboutComputerSystemIntoMap() {
                final ComputerSystem computerSystem = OshiUsageClass.HardwareSubClass.getOshiComputerSystem();
                return Map.of(
                        BasicStructuresClass.STR_SYSTEM + " " + BasicStructuresClass.STR_MANUFACTURER, computerSystem.getManufacturer(),
                        BasicStructuresClass.STR_SYSTEM + " " + BasicStructuresClass.STR_MODEL, computerSystem.getModel(),
                        BasicStructuresClass.STR_SYSTEM + " " + BasicStructuresClass.STR_SRL_NUM, computerSystem.getSerialNumber());
            }

            /**
             * capture Firmware parameters into Map
             * @return Map
             */
            private static Map<String, Object> getDetailsAboutFirmwareIntoMap() {
                final Firmware firmware = OshiUsageClass.HardwareSubClass.getOshiFirmware();
                return Map.of(
                        BasicStructuresClass.STR_FIRMWARE + " " + BasicStructuresClass.STR_MANUFACTURER, firmware.getManufacturer(),
                        BasicStructuresClass.STR_FIRMWARE + " " + "Name", firmware.getName(),
                        BasicStructuresClass.STR_FIRMWARE + " " + "Description", firmware.getDescription(),
                        BasicStructuresClass.STR_FIRMWARE + " " + BasicStructuresClass.STR_VERSION, firmware.getVersion(),
                        BasicStructuresClass.STR_FIRMWARE + " " + "Release Date", firmware.getReleaseDate() == null ? "unknown" : firmware.getReleaseDate());
            }

            /**
             * Mainboard details gathered
             * @return Map
             */
            public static Map<String, Object> getDetailsAboutMainboard() {
                final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>(getDetailsAboutMotherboardIntoMap());
                arrayAttributes.putAll(getDetailsAboutFirmwareIntoMap());
                arrayAttributes.putAll(getDetailsAboutComputerSystemIntoMap());
                return arrayAttributes;
            }

            /**
             * capture Motherboard parameters into Map
             * @return Map
             */
            private static Map<String, Object> getDetailsAboutMotherboardIntoMap() {
                final Baseboard baseboard = OshiUsageClass.HardwareSubClass.getOshiMotherboard();
                return Map.of(
                        BasicStructuresClass.STR_MAINBOARD + " " + BasicStructuresClass.STR_MANUFACTURER, baseboard.getManufacturer(),
                        BasicStructuresClass.STR_MAINBOARD + " " + BasicStructuresClass.STR_MODEL, baseboard.getModel(),
                        BasicStructuresClass.STR_MAINBOARD + " " + BasicStructuresClass.STR_VERSION, baseboard.getVersion(),
                        BasicStructuresClass.STR_MAINBOARD + " " + BasicStructuresClass.STR_SRL_NUM, baseboard.getSerialNumber());
            }
        }

    }

}
