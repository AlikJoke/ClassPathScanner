package ru.joke.classpath.services.internal;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapDictionaryTest {

    @Test
    void map() {
        final var map = Map.of("0", "test", "1", "qq");
        final var dictionary = new MapDictionary(map);

        assertEquals("test", dictionary.map("0"), "Value must be equal");
        assertEquals("qq", dictionary.map("1"), "Value must be equal");
        assertNull(dictionary.map("2"), "Value must be null");
    }

    @Test
    void addMapping() {
        final var dictionary = new MapDictionary(new HashMap<>());
        assertNull(dictionary.map("0"), "Value must be null");

        dictionary.addMapping("0", "test");
        assertEquals("test", dictionary.map("0"), "Value must be equal");

        dictionary.addMapping("0", "qq");
        assertEquals("test", dictionary.map("0"), "Value must be equal");
    }

    @Test
    void toMap() {
        final var dictionary = new MapDictionary(new HashMap<>());
        dictionary.addMapping("0", "test");
        dictionary.addMapping("1", "qq");

        assertEquals(Map.of("0", "test", "1", "qq"), dictionary.toMap(), "Map must be equal");
    }

    @Test
    void size() {
        final var dictionary = new MapDictionary(new HashMap<>());
        dictionary.addMapping("0", "test");
        dictionary.addMapping("1", "qq");

        assertEquals(2, dictionary.size(), "Size must be equal");
    }

    @Test
    void reverseDictionary() {
        final var map = Map.of("0", "test", "1", "qq");
        final var dictionary = new MapDictionary(map);

        final var reversedDictionary = dictionary.reversedDictionary();
        assertNotNull(reversedDictionary, "Reversed dictionary must be not null");
        assertEquals(2, reversedDictionary.size(), "Size of reversed map must be equal");
        assertEquals("0", reversedDictionary.map("test"), "Value must be equal");
        assertEquals("1", reversedDictionary.map("qq"), "Value must be equal");
    }
}