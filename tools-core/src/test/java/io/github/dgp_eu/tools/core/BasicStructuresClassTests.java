package io.github.dgp_eu.tools.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Testing for BasicStructuresClass
 */
@DisplayName("BasicStructuresClass testing")
class BasicStructuresClassTests {
    /** String for Original not equal to Expected */
    private static final String ORIG_NQ_EXPCT = "\"%s\" is not equal to \"%s\"";
    /** Constant for first */
    private static final String STR_FIRST = "first";

    @Test
    @DisplayName("Simple test to verify that 51 is same as 51 divided by 100")
    void testComputePercentageSafelySimple() {
        final BigDecimal original = new BigDecimal(51).setScale(2, RoundingMode.HALF_UP);
        final BigDecimal handled = BasicStructuresClass.computePercentageSafely(51, 100);
        assertEquals(original, handled, String.format(ORIG_NQ_EXPCT, handled, original));
    }

    @Test
    @DisplayName("Simple test to verify that 0 is same as 51 divided by 0")
    void testComputePercentageSafelyZeroDivision() {
        final BigDecimal original = new BigDecimal(55).setScale(2, RoundingMode.HALF_UP);
        final BigDecimal handled = BasicStructuresClass.computePercentageSafely(55, 0);
        assertEquals(original, handled, String.format(ORIG_NQ_EXPCT, handled, original));
    }

    @Test
    @DisplayName("Simple test to verify that 51.123 is not the same as 51.123 divided by 1000")
    void testComputePercentageSafelyNotEnoughPrecision() {
        final BigDecimal expected = new BigDecimal(51.123);
        final BigDecimal handled = BasicStructuresClass.computePercentageSafely(51_123L, 100_000L);
        assertNotEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    @DisplayName("Simple test to verify that 2026 is same as converted value from String 2026")
    void testConvertStringIntoBigDecimal() {
        final BigDecimal expected = new BigDecimal("2026");
        final BigDecimal handled = BasicStructuresClass.convertStringIntoBigDecimal("2026");
        assertEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    @DisplayName("Simple test to verify that 2026 is not the same as converted value from String 01.01.2026")
    void testConvertStringIntoBigDecimalInvalid() {
        final BigDecimal expected = new BigDecimal("01.2026");
        final BigDecimal handled = BasicStructuresClass.convertStringIntoBigDecimal("01.01.2026");
        assertNotEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    @DisplayName("Simple test to verify that 20 is same as Integer 20")
    void testConvertStringIntoInteger() {
        final int expected = 20;
        final int handled = BasicStructuresClass.convertStringIntoInteger("20");
        assertEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    @DisplayName("Simple test to verify that 2026 is not the same as converted value from String 01.01.2026")
    void testConvertStringIntoIntegerInvalid() {
        assertNotEquals(2_026, BasicStructuresClass.convertStringIntoInteger("01.01.2026"));
    }

    @Test
    @DisplayName("Counting 3 named parameters within a dummy query")
    void testCountNamedParametersWithinQuery() {
        final String strHaystack = "SELECT {Field1} FROM table WHERE {Field 2} = {Value_to_filter};";
        final int intOriginal = 3;
        final int handled = BasicStructuresClass.countNamedParametersWithinQuery(strHaystack);
        assertEquals(intOriginal, handled, String.format(ORIG_NQ_EXPCT, handled, intOriginal));
    }

    @Test
    @DisplayName("Counting 3 positional type parameters within a dummy query")
    void testCountPositionalTypeParametersWithinQuery() {
        final String strHaystack = "SELECT %s FROM table WHERE %s = %d;";
        final int intOriginal = 3;
        final int handled = BasicStructuresClass.countPositionalTypeParametersWithinQuery(strHaystack);
        assertEquals(intOriginal, handled, String.format(ORIG_NQ_EXPCT, handled, intOriginal));
    }

    /**
     * Test for StringCleaningClass
     */
    @Nested
    /* default */ @DisplayName("StringCleaningClass testing...")
    class TestStringCleaningSubClass {

        @Test
        void testCleanStringAsDatabaseObject() {
            final String strOriginal = "Original1";
            final String handled = BasicStructuresClass.StringCleaningSubClass.cleanStringAsDatabaseObject(strOriginal + "^");
            assertEquals(strOriginal, handled, String.format(ORIG_NQ_EXPCT, handled, strOriginal));
        }

        @Test
        void testCleanStringFromCurlyBraces() {
            final String strOriginal = "Original2";
            final String handled = BasicStructuresClass.StringCleaningSubClass.cleanStringFromCurlyBraces("{" + strOriginal + "}");
            assertEquals(strOriginal, handled, String.format(ORIG_NQ_EXPCT, handled, strOriginal));
        }

        @Test
        void testCleanStringFromUnwantedCharacters() {
            final String strOriginal = "Original3";
            final String handled = BasicStructuresClass.StringCleaningSubClass.cleanStringFromUnwantedCharacters(strOriginal + "^");
            assertEquals(strOriginal, handled, String.format(ORIG_NQ_EXPCT, handled, strOriginal));
        }

        @Test
        void testStripQuotes() {
            final String strOriginal = "Original4";
            final String handled = BasicStructuresClass.StringCleaningSubClass.stripQuotes("\"" + strOriginal + "\"");
            assertEquals(strOriginal, handled, String.format(ORIG_NQ_EXPCT, handled, strOriginal));
        }

        @Test
        void testStripQuotesShort() {
            final String strOriginal = "O";
            final String handled = BasicStructuresClass.StringCleaningSubClass.stripQuotes("\"");
            assertNotEquals(strOriginal, handled, String.format(ORIG_NQ_EXPCT, handled, strOriginal));
        }

        /**
         * Constructor
         */
        TestStringCleaningSubClass() {
            // intentionally blank
        }

    }

    /**
     * Test for StringEvaluationClass
     */
    @Nested
    /* default */ @DisplayName("TestStringEvaluationClass testing...")
    class TestStringEvaluationSubClass {

        @Test
        void testHasMatchingSubstring() {
            final List<String> listStrings = new ArrayList<>();
            listStrings.add(STR_FIRST);
            listStrings.add("Second");
            final boolean handled = BasicStructuresClass.StringEvaluationSubClass.hasMatchingSubstring(STR_FIRST, listStrings);
            assertTrue(handled, String.format("\"%s\" is not true as expected", handled));
        }

        /**
         * Constructor
         */
        TestStringEvaluationSubClass() {
            // intentionally blank
        }

    }

    /**
     * Test for StringConversionClass
     */
    @Nested
    /* default */ @DisplayName("StringConversionClass testing...")
    class TestStringConversionSubClass {

        @Test
        void testConvertPromptParametersIntoNamedParameters() {
            final String strOriginal = "SELECT {Field A}";
            final String strExpected = "SELECT :Field_A";
            final String handled = BasicStructuresClass.StringConversionSubClass.convertPromptParametersIntoNamedParameters(strOriginal);
            assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
        }

        @Test
        void testConvertPromptParametersIntoParameters() {
            final String strOriginal = "SELECT {Field A}";
            final String strExpected = "SELECT ?";
            final String handled = BasicStructuresClass.StringConversionSubClass.convertPromptParametersIntoParameters(strOriginal);
            assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
        }

        /**
         * Constructor
         */
        TestStringConversionSubClass() {
            // intentionally blank
        }

    }

    /**
     * Test for StringEvaluationClass
     */
    @Nested
    /* default */ @DisplayName("ListAndMapSubClass testing...")
    class TestListAndMapSubClass {

        @Test
        @DisplayName("assertAll: Get word counts from list with space separator returns correct counts")
        void testGetWordCountsWithSpaceSeparator() {
            final List<String> valList = new ArrayList<>();
            valList.add("pear banana pear");
            valList.add("banana cherry pear");
            final SequencedMap<String, Long> wordCounts = BasicStructuresClass.ListAndMapSubClass.getWordCounts(valList, " ");
            assertAll("Get word counts from list with space separator returns correct counts",
                    () -> assertEquals(3, wordCounts.get("pear"), String.format(ORIG_NQ_EXPCT, wordCounts.get("pear"), 3)),
                    () -> assertEquals(2, wordCounts.get("banana"), String.format(ORIG_NQ_EXPCT, wordCounts.get("banana"), 2)),
                    () -> assertEquals(1, wordCounts.get("cherry"), String.format(ORIG_NQ_EXPCT, wordCounts.get("cherry"), 1))
            );
        }

        @Test
        @DisplayName("Get word counts returns empty map for empty list")
        void testGetWordCountsWithEmptyListReturnsEmpty() {
            final List<String> valList = new ArrayList<>();
            final SequencedMap<String, Long> wordCounts = BasicStructuresClass.ListAndMapSubClass.getWordCounts(valList, " ");
            assertTrue(wordCounts.isEmpty(), "Word counts should be empty for empty input list");
        }

        @Test
        @DisplayName("Get word counts orders words by frequency descending")
        void testGetWordCountsOrdersByFrequencyDescending() {
            final List<String> valList = new ArrayList<>();
            valList.add("a a a b b c");
            final SequencedMap<String, Long> wordCounts = BasicStructuresClass.ListAndMapSubClass.getWordCounts(valList, " ");
            final List<String> keys = new ArrayList<>(wordCounts.keySet());
            assertAll("Get word counts orders words by frequency descending",
                    () -> assertEquals("a", keys.get(0), "Most frequent word should be first"),
                    () -> assertEquals("b", keys.get(1), "Second frequent word should be second"),
                    () -> assertEquals("c", keys.get(2), "Least frequent word should be last")
            );
        }

        @Test
        @DisplayName("Convert map of strings into list of properties creates correct structure")
        void convertMapOfStringsIntoListOfPropertiesCreatesCorrectStructure() {
            final Map<String, Object> inMap = new ConcurrentHashMap<>();
            inMap.put("key11", "value11");
            inMap.put("key2", "value2");
            final List<Properties> result = BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("TestCategory", inMap);
            assertAll("Convert map of strings into list of properties creates correct structure",
                    () -> assertEquals(2, result.size(), "Result should have 2 property objects"),
                    () -> assertEquals("TestCategory", result.get(0).getProperty("Category"), "Category should be set")
            );
        }

        @Test
        @DisplayName("Convert map of strings into list of properties sorts by element name")
        void convertMapOfStringsIntoListOfPropertiesSortsByElementName() {
            final Map<String, Object> inMap = new ConcurrentHashMap<>();
            inMap.put("zebra", "value1");
            inMap.put("apple", "value2");
            final List<Properties> result = BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Cat", inMap);
            assertEquals("apple", result.get(0).getProperty("Element"), "Should be sorted alphabetically");
        }

        @Test
        @DisplayName("Merge keys preserves non-merged keys")
        void mergeKeysPreservesNonMergedKeys() {
            final Map<String, List<String>> inputMap = new ConcurrentHashMap<>();
            inputMap.put("a", new ArrayList<>(Arrays.asList("v1")));
            inputMap.put("b", new ArrayList<>(Arrays.asList("v2")));
            final Map<List<String>, String> mergeRules = new ConcurrentHashMap<>();
            mergeRules.put(Arrays.asList("a"), "merged");
            final Map<String, List<String>> result = BasicStructuresClass.ListAndMapSubClass.mergeKeys(inputMap, mergeRules);
            assertTrue(result.containsKey("b"), "Non-merged key should be preserved");
        }

        @Test
        @DisplayName("Sort properties maintains order specified in list")
        void sortPropertiesMaintainsOrderSpecifiedInList() {
            final Properties prop = new Properties();
            prop.put("third", "value3");
            prop.put(STR_FIRST, "value1");
            prop.put("second", "value2");
            final List<String> order = Arrays.asList(STR_FIRST, "second", "third");
            final SequencedMap<Object, Object> result = BasicStructuresClass.ListAndMapSubClass.sortProperties(prop, order);
            final List<Object> keys = new ArrayList<>(result.keySet());
            assertAll("Sort properties maintains order specified in list",
                    () -> assertEquals(STR_FIRST, keys.get(0), "First key should be ordered first"),
                    () -> assertEquals("second", keys.get(1), "Second key should be ordered second")
            );
        }

        @Test
        @DisplayName("Sort properties puts unspecified keys at end")
        void sortPropertiesPutsUnspecifiedKeysAtEnd() {
            final Properties prop = new Properties();
            prop.put("unspecified", "value");
            prop.put(STR_FIRST, "value1");
            final List<String> order = Arrays.asList(STR_FIRST);
            final SequencedMap<Object, Object> result = BasicStructuresClass.ListAndMapSubClass.sortProperties(prop, order);
            final List<Object> keys = new ArrayList<>(result.keySet());
            assertAll("Sort properties puts unspecified keys at end",
                    () -> assertEquals(STR_FIRST, keys.get(0), "Specified key should come first"),
                    () -> assertEquals("unspecified", keys.get(1), "Unspecified key should come last")
            );
        }

        /**
         * Constructor
         */
        TestListAndMapSubClass() {
            // intentionally blank
        }

    }

    /**
     * Test for StringEvaluationClass
     */
    @Nested
    /* default */ @DisplayName("StringTransformationClass testing...")
    class TestStringTransformationClass {

        @Test
        @DisplayName("Compute string signature produces consistent output")
        void computeStringSignatureProducesConsistentOutput() {
            final String input = "test_input";
            final String first = BasicStructuresClass.StringTransformationSubClass.computeStringSignature(input);
            final String second = BasicStructuresClass.StringTransformationSubClass.computeStringSignature(input);
            assertEquals(first, second, "Same input should produce same signature");
        }

        @Test
        @DisplayName("Compute string signature produces different output for different inputs")
        void computeStringSignatureProducesDifferentOutputForDifferentInputs() {
            final String sig1 = BasicStructuresClass.StringTransformationSubClass.computeStringSignature("input1");
            final String sig2 = BasicStructuresClass.StringTransformationSubClass.computeStringSignature("input2");
            assertNotEquals(sig1, sig2, "Different inputs should produce different signatures");
        }

        @Test
        @DisplayName("Compute string signature produces non-empty output")
        void computeStringSignatureProducesNonEmptyOutput() {
            final String signature = BasicStructuresClass.StringTransformationSubClass.computeStringSignature("test");
            assertFalse(signature.isEmpty(), "Signature should not be empty");
        }

        @Test
        @DisplayName("Enclose string if contains space adds quotes when space present")
        void encloseStringIfContainsSpaceAddsQuotesWhenSpacePresent() {
            final String result = BasicStructuresClass.StringTransformationSubClass.encloseStringIfContainsSpace("hello world", '"');
            assertEquals("\"hello world\"", result, String.format(ORIG_NQ_EXPCT, result, "\"hello world\""));
        }

        @Test
        @DisplayName("Enclose string if contains space leaves unchanged when no space")
        void encloseStringIfContainsSpaceLeaveUnchangedWhenNoSpace() {
            final String result = BasicStructuresClass.StringTransformationSubClass.encloseStringIfContainsSpace("helloworld", '"');
            assertEquals("helloworld", result, String.format(ORIG_NQ_EXPCT, result, "helloworld"));
        }

        @Test
        void testEncloseStringIfContainsSpace() {
            final String strOriginal = "Original String";
            final String strExpected = "\"Original String\"";
            final String handled = BasicStructuresClass.StringTransformationSubClass.encloseStringIfContainsSpace(strOriginal, '\"');
            assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
        }

        @Test
        void testEncloseStringIfContainsSpaceFutile() {
            final String strExpected = "\"Original String\"";
            final String handled = BasicStructuresClass.StringTransformationSubClass.encloseStringIfContainsSpace(strExpected, '\"');
            assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
        }

        @Test
        void testEncloseStringIfContainsSpacePartialEnd() {
            final String strOriginal = "\"Original String";
            final String strExpected = strOriginal + '"';
            final String handled = BasicStructuresClass.StringTransformationSubClass.encloseStringIfContainsSpace(strOriginal, '\"');
            assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
        }

        @Test
        void testEncloseStringIfContainsSpacePartialStart() {
            final String strOriginal = "Original String\"";
            final String strExpected = '"' + strOriginal;
            final String handled = BasicStructuresClass.StringTransformationSubClass.encloseStringIfContainsSpace(strOriginal, '\"');
            assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
        }

        @Test
        void testObfuscateProperties() {
            final Properties originalProps = new Properties();
            originalProps.put("key", "value");
            originalProps.put("password", "password");
            final Properties expectedProps = new Properties();
            expectedProps.put("key", "value");
            expectedProps.put("password", "*U*N*D*I*S*C*L*O*S*E*D*");
            final Properties handled = BasicStructuresClass.StringTransformationSubClass.obfuscateProperties(originalProps);
            assertEquals(expectedProps, handled, String.format(ORIG_NQ_EXPCT, handled, expectedProps));
        }

        /**
         * Constructor
         */
        TestStringTransformationClass() {
            // intentionally blank
        }

    }

    /**
     * Constructor
     */
    BasicStructuresClassTests() {
        // intentionally blank
    }

}
