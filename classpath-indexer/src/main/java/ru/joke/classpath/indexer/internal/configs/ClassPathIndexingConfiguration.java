package ru.joke.classpath.indexer.internal.configs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Indexing configuration, formed based on a configuration file from the library's consumer module.
 *
 * @param annotations annotations that should be indexed; resources annotated by these annotations are also indexed;
 *                    if an annotation is annotated, the same indexing rules apply to the child annotation recursively;
 *                    cannot be {@code null}.
 * @param interfaces  interfaces that should be indexed; all implementations of these interfaces and all interfaces
 *                    extending them (recursively) are also indexed; cannot be {@code null}.
 * @param aliases     resources with specified aliases; only the resources themselves are indexed, without recursive
 *                    application of indexing rules; cannot be {@code null}.
 * @param classes     classes that should be indexed; all subclasses of these classes (at any level of inheritance)
 *                    are also indexed; cannot be {@code null}.
 *
 * @author Alik
 * @see ClassPathIndexingConfigurationService
 */
public record ClassPathIndexingConfiguration (
        Set<String> annotations,
        Set<String> interfaces,
        Map<String, Set<String>> aliases,
        Set<String> classes
) {

    private static final String ANNOTATIONS_TAG = "#annotations";
    private static final String INTERFACES_TAG = "#interfaces";
    private static final String CLASSES_TAG = "#classes";
    private static final String ALIASES_TAG = "#aliases";

    private static final int ANNOTATIONS_TYPE = 0;
    private static final int INTERFACES_TYPE = 1;
    private static final int CLASSES_TYPE = 2;
    private static final int ALIASES_TYPE = 3;

    /**
     * Parses the configuration file into an indexing configuration object.
     *
     * @param configStream configuration file stream; cannot be {@code null}.
     * @return created configuration object; cannot be {@code null}.
     * @throws IOException in case of I/O errors when working with the file stream
     */
    public static ClassPathIndexingConfiguration parse(final InputStream configStream) throws IOException {
        final Set<String> annotations = new HashSet<>();
        final Set<String> interfaces = new HashSet<>();
        final Set<String> classes = new HashSet<>();
        final Map<String, Set<String>> aliases = new HashMap<>();

        final List<String> configs = new ArrayList<>();
        try (final var configReader = new BufferedReader(new InputStreamReader(configStream, StandardCharsets.UTF_8))) {

            var line = configReader.readLine();
            while (line != null) {
                configs.add(line);
                line = configReader.readLine();
            }
        }

        int lastType = -1;
        for (var config : configs) {
            switch (config) {
                case ANNOTATIONS_TAG -> lastType = ANNOTATIONS_TYPE;
                case INTERFACES_TAG -> lastType = INTERFACES_TYPE;
                case CLASSES_TAG -> lastType = CLASSES_TYPE;
                case ALIASES_TAG -> lastType = ALIASES_TYPE;
                case "" -> {}
                default -> {
                    switch (lastType) {
                        case ANNOTATIONS_TYPE -> annotations.add(config);
                        case INTERFACES_TYPE -> interfaces.add(config);
                        case CLASSES_TYPE -> classes.add(config);
                        case ALIASES_TYPE -> {
                            final var resourceAliases = parseAliases(config);
                            aliases.computeIfAbsent(
                                    resourceAliases.getKey(),
                                    k -> new HashSet<>()
                            ).addAll(resourceAliases.getValue());
                        }
                    }
                }
            }
        }

        return new ClassPathIndexingConfiguration(
                Collections.unmodifiableSet(annotations),
                Collections.unmodifiableSet(interfaces),
                Collections.unmodifiableMap(aliases),
                Collections.unmodifiableSet(classes)
        );
    }

    private static Map.Entry<String, Set<String>> parseAliases(final String config) {
        final var parts = config.split(":");
        return Map.entry(
                parts[0],
                Arrays.stream(parts[1].split(";"))
                        .collect(Collectors.toSet())
        );
    }
}
