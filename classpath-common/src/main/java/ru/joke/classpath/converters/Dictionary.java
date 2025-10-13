package ru.joke.classpath.converters;

import java.util.Map;
import java.util.Set;

/**
 * An abstraction of a lookup table that allows assigning aliases to different resource
 * components to reduce the final index size.
 *
 * @author Alik
 * @see ClassPathResourceConverter
 */
public interface Dictionary {

    /**
     * The separator used for values and their aliases when represented as strings for writing to the index.
     */
    String MAP_SEPARATOR = ":";

    /**
     * Returns the alias for the given value.
     *
     * @param str value; can be {@code null}.
     * @return the alias; can be {@code null} if no mapping present.
     */
    String map(String str);

    /**
     * Adds a mapping between the given key (alias) and value.
     *
     * @param key key of the mapping; can not be {@code null}.
     * @param value value of the mapping; cannot be {@code null}.
     */
    void addMapping(String key, String value);

    /**
     * Returns the lookup table as an associative array.
     *
     * @return map representation of the lookup table; cannot be {@code null}.
     */
    Map<String, String> toMap();

    /**
     * Returns the size of the lookup table (the count of mappings).
     *
     * @return the size of the dictionary.
     */
    int size();

    /**
     * Returns the reversed lookup table of this one.
     *
     * @return reversed dictionary; cannot be {@code null}.
     */
    Dictionary reversedDictionary();

    /**
     * Populates the lookup table based on a given set of string representations of its elements.
     *
     * @param mappings string representations; cannot be {@code null}.
     */
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

    /**
     * Converts the given lookup table to a string using the specified separator.
     *
     * @param dictionary target lookup table (dictionary); cannot be {@code null}.
     * @param separator separator of the mappings; cannot be {@code null}.
     * @return string representation of the dictionary (lookup table); cannot be {@code null}.
     */
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
