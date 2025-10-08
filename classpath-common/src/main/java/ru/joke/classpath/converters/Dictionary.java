package ru.joke.classpath.converters;

import java.util.Map;
import java.util.Set;

public interface Dictionary {

    String MAP_SEPARATOR = ":";

    String map(String str);

    void addMapping(String key, String value);

    Map<String, String> toMap();

    int size();

    Dictionary reversedDictionary();

    default void fill(Set<String> mappings) {
        mappings.forEach(m -> {
            final var sepIdx = m.lastIndexOf(MAP_SEPARATOR);
            if (sepIdx == -1) {
                return;
            }

            final var key = m.substring(0, sepIdx);
            final var value = m.substring(sepIdx + 1);
            addMapping(key, value);
        });
    }

    static String toString(Dictionary dictionary, String separator) {
        final var builder = new StringBuilder();
        dictionary
                .toMap()
                .forEach(
                        (key, value) -> builder
                                            .append(key)
                                            .append(MAP_SEPARATOR)
                                            .append(value)
                                            .append(separator)
                );

        return builder.toString();
    }
}
