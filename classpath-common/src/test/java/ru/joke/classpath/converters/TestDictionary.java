package ru.joke.classpath.converters;

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
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> toMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dictionary reverseDictionary() {
        final var reversedMap =
                this.map.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        return new TestDictionary(reversedMap);
    }
}
