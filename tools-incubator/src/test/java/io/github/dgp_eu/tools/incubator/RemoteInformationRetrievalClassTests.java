package io.github.dgp_eu.tools.incubator;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.dgp_eu.tools.core.BasicStructuresClass;

/**
 * Testing for RemoteInformationRetrievalClass
 */
class RemoteInformationRetrievalClassTests {

    @Test
    @DisplayName("Testing if latest Maven package version is a String as a version pattern")
    void testGetLatestVersionFromMavenCentralRepository() {
        final String strPackage = "com.github.oshi:oshi-core-ffm";
        final String handled = RemoteInformationRetrievalClass.getLatestVersionFromMavenCentralRepository(strPackage);
        final boolean isVersion = BasicStructuresClass.StringEvaluationSubClass.isStringActuallyVersion(handled);
        assertAll("Testing if latest Maven package version is a String as a version pattern",
                () -> assertNotNull(handled, String.format("Latest Version should not be null... %s", handled)),
                () -> assertTrue(isVersion, String.format("Environment gathering logic should not be null... %s", handled))
        );
    }

    /**
     * Constructor
     */
    RemoteInformationRetrievalClassTests() {
        // intentionally blank
    }

}
