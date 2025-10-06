package ru.joke.classpath.services.internal;

import ru.joke.classpath.converters.Dictionary;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

final class MapDictionary implements Dictionary {

    private final Map<String, String> dictionary;

    MapDictionary(Map<String, String> dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public String map(String str) {
        return this.dictionary.get(str);
    }

    @Override
    public void addMapping(String key, String value) {
        this.dictionary.putIfAbsent(key, value);
    }

    @Override
    public Map<String, String> toMap() {
        return Collections.unmodifiableMap(this.dictionary);
    }

    @Override
    public int size() {
        return this.dictionary.size();
    }

    @Override
    public Dictionary reverseDictionary() {
        final var reversedMap =
                this.dictionary.entrySet()
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        return new MapDictionary(reversedMap);
    }
}
