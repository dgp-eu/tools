/**
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.dgp_eu.tools.core;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * exposing things to Log
 */
public final class LogExposureClass {

    /** Logger variable */
    public static final Logger LOGGER = LoggerFactory.getLogger(
        LogExposureClass.class
    );
    /** standard Unknown feature constant */
    public static final String STR_I18N_UNKN_FTS =
        "Feature %s is NOT known in %s...";
    /** standard Unknown constant */
    public static final String STR_I18N_UNKN = "Unknown";

    /**
     * Constructor
     */
    private LogExposureClass() {
        super();
    }

    /**
     * Build message for I/O exception
     * @param inStackTrace tracking back the Stack Trace
     */
    public static void exposeInputOutputException(final String inStackTrace) {
        final String strFeedbackErr = String.format(
            "Input/Output exception on... %s",
            inStackTrace
        );
        LOGGER.error(strFeedbackErr);
    }

    /**
     * Build message for I/O exception
     * @param customMsg custom message
     * @param inStackTrace tracking back the Stack Trace
     */
    public static void exposeInputOutputException(
        final String customMsg,
        final String inStackTrace
    ) {
        final String strFeedbackErr =
            customMsg + String.format("... %s", inStackTrace);
        LOGGER.error(strFeedbackErr);
    }

    /**
     * Log Process Builder command conditionally
     * @param strCommand command to execute
     */
    public static void exposeProcessBuilder(final String strCommand) {
        final String strFeedback = String.format(
            "I intend to execute following shell command %s",
            strCommand
        );
        LOGGER.debug(strFeedback);
    }

    /**
     * Log Process Builder command conditionally
     * @param inStackTrace tracking back the Stack Trace
     */
    public static void exposeProjectModel(final String inStackTrace) {
        final String strFeedback = String.format(
            "Error on getting project model... \"%s\"",
            inStackTrace
        );
        LOGGER.error(strFeedback);
    }

    /**
     * Build message for file operation error
     * @param strFileName file name
     * @param strStagTrace stag trace
     * @return message for file operation error
     */
    public static String getFileErrorMessage(
        final String strFileName,
        final String strStagTrace
    ) {
        return String.format(
            "Error encountered when attempting to write to %s file... %s",
            strFileName,
            strStagTrace
        );
    }

    /**
     * handle NameUnformatted
     * @param intRsParams number for parameters
     * @param strUnformatted original string
     * @param strReplacement replacements (1 to multiple)
     * @return String
     */
    public static String handleNameUnformattedMessage(
        final int intRsParams,
        final String strUnformatted,
        final Object... strReplacement
    ) {
        return switch (intRsParams) {
            case 1 -> String.format(strUnformatted, strReplacement[0]);
            case 2 -> String.format(
                strUnformatted,
                strReplacement[0],
                strReplacement[1]
            );
            case 3 -> String.format(
                strUnformatted,
                strReplacement[0],
                strReplacement[1],
                strReplacement[2]
            );
            default -> getUnsupportedFeatures(
                String.valueOf(intRsParams),
                StackWalker.getInstance().walk(frames ->
                    frames
                        .findFirst()
                        .map(
                            frame ->
                                frame.getClassName() +
                                "." +
                                frame.getMethodName()
                        )
                        .orElse(STR_I18N_UNKN)
                )
            );
        };
    }

    /**
     * get Unsupported Feature
     * @param strDecision decision evaluated
     * @param strWhere which function this is called from
     * @return String with localized feedback
     */
    public static String getUnsupportedFeatures(
        final String strDecision,
        final String strWhere
    ) {
        final String strFeedbackErr = String.format(
            STR_I18N_UNKN_FTS,
            strDecision,
            strWhere
        );
        LOGGER.error(strFeedbackErr);
        return strFeedbackErr;
    }

    /**
     * Configuration management
     */
    public static final class ConfigurationSubClass {

        /** Log builder */
        private static final ConfigurationBuilder<BuiltConfiguration> BUILDER =
            ConfigurationBuilderFactory.newConfigurationBuilder();
        /** PatternLayout variable */
        private static final LayoutComponentBuilder PATTERN_LAYOUT =
            BUILDER.newLayout("PatternLayout").addAttribute(
                "pattern",
                "%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ} [%-6p] %C{3}.%M(%F:%L) - %m%n"
            );
        /** default Log Level */
        /*/ default */ private static Level logLevel = Level.INFO;
        /** default Log location and base-namel */
        /*/ default */ private static String logFile = "log/DGP-EU_Tools-";

        // Private constructor to prevent instantiation
        private ConfigurationSubClass() {
            // intentionally blank
        }

        /**
         * Initiate Log
         * @param inputFile input Log file
         */
        public static void initiate(final String inputFile) {
            logFile = inputFile;
            buildRollingFile("rest");
            buildRollingFile(BasicStructuresClass.STR_ERROR);
            final RootLoggerComponentBuilder rootLogger = BUILDER.newRootLogger(
                logLevel
            )
                .add(BUILDER.newAppenderRef("rollingRest"))
                .add(BUILDER.newAppenderRef("rollingError"))
                .addAttribute("additivity", false);
            BUILDER.add(rootLogger);
            Configurator.initialize(BUILDER.build());
        }

        private static void buildRollingFile(final String strType) {
            final String logFileName = logFile + strType + ".log";
            final String filePattern = logFileName.replace(
                ".log",
                "%d{yyyy-MM-dd-HH}-%i.log"
            );
            final ComponentBuilder<?> policy = buildPolicies();
            final String rollingName = switch (strType) {
                case BasicStructuresClass.STR_ERROR -> "rollingError";
                case "rest" -> "rollingRest";
                default -> "rollingAll";
            };
            final FilterComponentBuilder levelRangeFilter = switch (strType) {
                case BasicStructuresClass.STR_ERROR -> buildLevelRangeFilter(
                            "FATAL",
                            "ERROR"
                    );
                case "rest" -> buildLevelRangeFilter(
                            "WARN",
                            "ALL"
                    );
                default -> buildLevelRangeFilter(
                            "FATAL",
                            "ALL"
                    );
            };
            final AppenderComponentBuilder rollingFile = BUILDER.newAppender(
                rollingName,
                "RollingFile"
            )
                .addAttribute("fileName", logFileName)
                .addAttribute("filePattern", filePattern)
                .add(PATTERN_LAYOUT)
                .addComponent(policy)
                .add(levelRangeFilter);
            BUILDER.add(rollingFile);
        }

        /**
         *
         * @param minLevel starting Level
         * @param maxLevel ending Level
         * @return LevelRangeFilter
         */
        private static FilterComponentBuilder buildLevelRangeFilter(
            final String minLevel,
            final String maxLevel
        ) {
            final FilterComponentBuilder flow = BUILDER.newFilter(
                "LevelRangeFilter",
                Filter.Result.ACCEPT,
                Filter.Result.DENY
            );
            flow.addAttribute("minLevel", minLevel);
            flow.addAttribute("maxLevel", maxLevel);
            return flow;
        }

        /**
         * Building Policies
         * @return ComponentBuilder
         */
        private static ComponentBuilder<?> buildPolicies() {
            final ComponentBuilder<?> triggeringPolicy = BUILDER.newComponent(
                "Policies"
            );
            final ComponentBuilder<?> timeBasedPolicy = BUILDER
                .newComponent("TimeBasedTriggeringPolicy")
                .addAttribute("interval", 1)
                .addAttribute("modulate", true);
            triggeringPolicy.addComponent(timeBasedPolicy);
            final ComponentBuilder<?> sizeBasedPolicy = BUILDER
                .newComponent("SizeBasedTriggeringPolicy")
                .addAttribute("size", "20M");
            triggeringPolicy.addComponent(sizeBasedPolicy);
            return triggeringPolicy;
        }

        /**
         * Setter for logLevel
         * @param inputLevel desired Level
         */
        public static void setLogLevel(final Level inputLevel) {
            logLevel = inputLevel;
        }

    }

}
