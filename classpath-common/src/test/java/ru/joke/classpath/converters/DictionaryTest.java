package ru.joke.classpath.converters;

import org.junit.jupiter.api.Test;
import ru.joke.classpath.test_util.TestDictionary;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DictionaryTest {

    @Test
    void testNotEmptyDictionaryToString() {
        final Map<String, String> map = new HashMap<>();
        map.put("0", "test");
        map.put("1", "qq");
        final var dictionary = new TestDictionary(map);
        final var result = Dictionary.toString(dictionary, ";");

        assertNotNull(result, "String must be not null");
        assertFalse(result.isEmpty(), "String must be not empty");
        assertEquals("0:test;1:qq;", result, "String representation must be equal");
    }

    @Test
    void testEmptyDictionaryToString() {
        final Map<String, String> map = new HashMap<>();
        final var dictionary = new TestDictionary(map);
        final var result = Dictionary.toString(dictionary, ";");

        assertNotNull(result, "String must be not null");
        assertTrue(result.isEmpty(), "String must be empty");
    }

    @Test
    void testFillDictionary() {
        final Map<String, String> map = new HashMap<>();
        final var dictionary = new TestDictionary(map);
        dictionary.fill(Set.of("0:test", "1:qq"));

        assertEquals("test", dictionary.map("0"), "Value must be equal");
        assertEquals("qq", dictionary.map("1"), "Value must be equal");
    }
}
