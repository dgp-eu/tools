package io.github.dgp_eu.tools.environment;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.dgp_eu.tools.core.ProjectClass;

/**
 * Testing for EnvironmentCapturingAssembleClass
 */
class EnvironmentCapturingAssembleClassTests {

    @Test
    @DisplayName("Simple test to check if environment details gathering results returns a valid JSON")
    void testPackageCurrentEnvironmentDetailsIntoJson() {
        ProjectClass.setPomFile("/tools-environment-pom.xml");
        final String handled = EnvironmentCapturingAssembleClass.packageCurrentEnvironmentDetailsIntoJson();
        assertNotNull(handled, String.format("JSON produced by environment gathering logic seem to be NULL... %s", handled));
    }

    @Test
    @DisplayName("Check if environment details gathering results is not null")
    void testPackageCurrentEnvironmentDetailsIntoListOfProperties() {
        ProjectClass.setPomFile("/tools-environment-pom.xml");
        final List<Properties> handled = EnvironmentCapturingAssembleClass.packageCurrentEnvironmentDetailsIntoListOfProperties();
        assertNotNull(handled, String.format("Environment gathering logic should not be null... %s", handled));
    }

    /**
     * Constructor
     */
    EnvironmentCapturingAssembleClassTests() {
        // intentionally blank
    }

}
