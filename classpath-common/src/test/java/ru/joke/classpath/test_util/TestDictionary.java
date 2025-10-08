package ru.joke.classpath.test_util;

import ru.joke.classpath.converters.Dictionary;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class TestDictionary implements Dictionary {

    private final Map<String, String> map;

    public TestDictionary(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public String map(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return map.get(str);
    }

    @Override
    public void addMapping(String key, String value) {
        this.map.put(key, value);
    }

    @Override
    public Map<String, String> toMap() {
        return Collections.unmodifiableMap(this.map);
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public Dictionary reversedDictionary() {
        final var reversedMap =
                this.map.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        return new TestDictionary(reversedMap);
    }
}
