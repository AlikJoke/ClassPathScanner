package ru.joke.classpath.services.internal;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AutoFillingDictionaryTest {

    @Test
    void map() {
        final Map<String, String> map = new HashMap<>();
        final var mapDictionary = new MapDictionary(map);
        final var dictionary = new AutoFillingDictionary(mapDictionary);

        assertTrue(dictionary.map("").isEmpty(), "Value must be empty");
        assertNull(dictionary.map(null), "Value must be null");
        assertEquals(0, dictionary.size(), "Dictionary must be empty");
        assertEquals("0", dictionary.map("test"), "Value must be null");
        assertEquals("1", dictionary.map("qq"), "Value must be null");
        assertEquals(2, dictionary.size(), "Dictionary size must be equal");
    }

    @Test
    void addMapping() {
        final var mapDictionary = new MapDictionary(new HashMap<>());
        final var dictionary = new AutoFillingDictionary(mapDictionary);
        dictionary.addMapping("0", "test");

        assertEquals("test", dictionary.map("0"), "Value must be not null");
    }

    @Test
    void toMap() {
        final var mapDictionary = new MapDictionary(new HashMap<>());
        mapDictionary.addMapping("0", "test");
        mapDictionary.addMapping("1", "qq");

        final var dictionary = new AutoFillingDictionary(mapDictionary);

        assertEquals(dictionary.toMap(), dictionary.toMap(), "Map must be equal");
    }

    @Test
    void size() {
        final var mapDictionary = new MapDictionary(new HashMap<>());
        mapDictionary.addMapping("0", "test");
        mapDictionary.addMapping("1", "qq");

        final var dictionary = new AutoFillingDictionary(mapDictionary);

        assertEquals(mapDictionary.size(), dictionary.size(), "Size must be equal");
    }

    @Test
    void reverseDictionary() {
        final var map = Map.of("0", "test", "1", "qq");
        final var mapDictionary = new MapDictionary(map);

        final var dictionary = new AutoFillingDictionary(mapDictionary);
        final var reversedDictionary = dictionary.reversedDictionary();

        assertNotNull(reversedDictionary, "Reversed dictionary must be not null");
        assertEquals(mapDictionary.reversedDictionary().toMap(), reversedDictionary.toMap(), "Reversed dictionary must be equal to origin reversed dictionary");
    }
}