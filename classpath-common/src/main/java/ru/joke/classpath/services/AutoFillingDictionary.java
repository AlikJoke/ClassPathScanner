package ru.joke.classpath.services;

import ru.joke.classpath.converters.Dictionary;

import java.util.Map;

final class AutoFillingDictionary implements Dictionary {

    private final Dictionary dictionary;

    AutoFillingDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public String map(String str) {
        var result = this.dictionary.map(str);
        if (result == null || result.isEmpty()) {
            result = String.valueOf(size());
            this.dictionary.addMapping(str, result);
        }

        return result;
    }

    @Override
    public void addMapping(String key, String value) {
        this.dictionary.addMapping(key, value);
    }

    @Override
    public Map<String, String> toMap() {
        return this.dictionary.toMap();
    }

    @Override
    public int size() {
        return this.dictionary.size();
    }

    @Override
    public Dictionary reverseDictionary() {
        return this.dictionary.reverseDictionary();
    }
}
