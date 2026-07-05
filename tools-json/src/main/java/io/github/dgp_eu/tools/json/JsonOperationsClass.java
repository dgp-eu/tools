/** Copyright 2026 Daniel-Gheorghe Popiniuc */
package io.github.dgp_eu.tools.json;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.github.dgp_eu.tools.core.LogExposureClass;
import io.github.dgp_eu.tools.core.RegularExpressionsClass;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * JSON handling
 */
public final class JsonOperationsClass {

    /**
     * check if input JSON is valid
     * @param inJson input JSON
     * @return true if valid JSON content is seen
     */
    public static boolean isJsonValid(final String inJson) {
        final ObjectMapper mapper = JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .build();
        boolean bolReturn = true;
        try {
            if (inJson == null) {
                bolReturn = false;
            } else {
                mapper.readTree(inJson);
            }
        } catch (JacksonException _) {
            bolReturn = false;
        }
        if (!bolReturn) {
            final String strFeedbackErr = String.format("input JSON is not valid, given was: %s", inJson);
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
        return bolReturn;
    }

    /**
     * Load all JSON nodes from String
     * 
     * @param strJson Input stream as source
     * @return JsonNode
     */
    public static JsonNode getJsonFileNodes(final InputStream strJson) {
        final JsonNode jsonRootNode;
        final ObjectMapper objectMapper = new ObjectMapper();
        jsonRootNode = objectMapper.readTree(strJson);
        final String strFeedback = String.format("JSON information has been loaded: %s", strJson);
        LogExposureClass.LOGGER.debug(strFeedback);
        return jsonRootNode;
    }

    /**
     * Load all JSON nodes from main configuration file
     * 
     * @param jsonFile file with expected JSON content
     * @return JsonNode
     */
    public static JsonNode getJsonFileNodes(final Path jsonFile) {
        final boolean validFile =
                RegularExpressionsClass.ValidationSubClass.isFileNameValid(jsonFile.getFileName().toString());
        if (!validFile) {
            final String strFeedback = String.format("Invalid file name: %s", jsonFile.getFileName().toString());
            LogExposureClass.LOGGER.error(strFeedback);
            throw new IllegalArgumentException("Invalid file name");
        }
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonRootNode = objectMapper.readTree(jsonFile);
        final String strFeedback = String.format("JSON information has been loaded from file %s", jsonFile.getFileName().toString());
        LogExposureClass.LOGGER.debug(strFeedback);
        return jsonRootNode;
    }

    /**
     * get Sub-node from Tree
     * 
     * @param givenJsonNode JSON node
     * @param strJsonNodeName name to search
     * @return JsonNode
     */
    private static JsonNode getJsonNodeFromTree(final JsonNode givenJsonNode, final String strJsonNodeName) {
        final String strFeedbackAtmpt = String.format("Will attempt to search for node named \"%s\"...", strJsonNodeName);
        LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
        final JsonNode jsonNode = givenJsonNode.at(strJsonNodeName);
        if (jsonNode.isMissingNode()) {
            final String strFeedback = String.format("Relevant JSON node \"%s\" was NOT found within \"%s\"...", strJsonNodeName, givenJsonNode);
            LogExposureClass.LOGGER.error(strFeedback);
        } else {
            final String strFeedback = String.format("Relevant JSON node \"%s\" was found!", strJsonNodeName);
            LogExposureClass.LOGGER.debug(strFeedback);
        }
        return jsonNode;
    }

    /**
     * Node into List of Properties
     * 
     * @param givenJsonNode JSON node
     * @param strJsonNodeName name to search
     * @return List of Properties
     */
    public static List<Properties> getJsonNodeNameListOfProperties(final JsonNode givenJsonNode, final String strJsonNodeName) {
        final List<Properties> listProperties = new ArrayList<>();
        final JsonNode jsonNode = getJsonNodeFromTree(givenJsonNode, strJsonNodeName);
        if(!jsonNode.isEmpty()) {
            jsonNode.forEach(arrayElement->{
                final Properties properties = new Properties();
                for (final Map.Entry<String, JsonNode> entry : arrayElement.properties()) {
                    properties.put(entry.getKey(), entry.getValue());
                }
                listProperties.add(properties);
            });
            setNodeRetrievingToDebugLog("List of Properties", strJsonNodeName, listProperties);
        }
        return listProperties;
    }

    /**
     * get list of String from a JSON node
     * 
     * @param givenJsonNode JSON node
     * @param strJsonNodeName name to search
     * @return List of String
     */
    public static List<String> getJsonNodeNameListOfStrings(final JsonNode givenJsonNode, final String strJsonNodeName) {
        final List<String> listStrings = new ArrayList<>();
        final JsonNode jsonNode = getJsonNodeFromTree(givenJsonNode, strJsonNodeName);
        if(!jsonNode.isEmpty()) {
            jsonNode.forEach(jsonSingleNode-> listStrings.add(jsonSingleNode.asString()));
            setNodeRetrievingToDebugLog("List of Strings", strJsonNodeName, listStrings);
        }
        return listStrings;
    }

    /**
     * Properties from a JSON node 
     * 
     * @param givenJsonNode JSON node
     * @param strJsonNodeName name to search
     * @return Properties
     */
    public static Properties getJsonNodeNameProperties(final JsonNode givenJsonNode, final String strJsonNodeName) {
        final JsonNode jsonNode = getJsonNodeFromTree(givenJsonNode, strJsonNodeName);
        final Properties properties = new Properties();
        if(!jsonNode.isEmpty()) {
            for (final Map.Entry<String, JsonNode> entry : jsonNode.properties()) {
                properties.put(entry.getKey(), entry.getValue());
            }
            setNodeRetrievingToDebugLog("Properties", strJsonNodeName, properties);
        }
        return properties;
    }

    /**
     * Single value from a JSON node
     * 
     * @param givenJsonNode JSON node
     * @param strJsonNode name to search
     * @return String
     */
    public static String getJsonValue(final JsonNode givenJsonNode, final String strJsonNode) {
        return getJsonNodeFromTree(givenJsonNode, strJsonNode).asString();
    }

    /**
     * Logging node retrieval activity to Debug Log
     * @param strWhat meaning of search
     * @param strJsonNodeName JSON node to look into
     * @param objValues values found
     */
    private static void setNodeRetrievingToDebugLog(final String strWhat, final String strJsonNodeName, final Object objValues) {
        final String strFeedback = String.format("For JSON node \"%s\" we found following %s: %s", strWhat, strJsonNodeName, objValues);
        LogExposureClass.LOGGER.debug(strFeedback);
    }

    /**
     * Constructor
     */
    private JsonOperationsClass() {
        // intentionally left blank
    }
}
