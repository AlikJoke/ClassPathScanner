package ru.joke.classpath.services;

import ru.joke.classpath.*;
import ru.joke.classpath.converters.ClassPathResourceConverter;
import ru.joke.classpath.converters.DelegateClassPathResourceConverter;
import ru.joke.classpath.converters.Dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

final class DefaultClassPathResourcesService implements ClassPathResourcesService {

    private static final String DICTIONARY_SEPARATOR = "#####";

    private final Map<String, ClassPathResources> resourcesByLocation;
    private final ClassPathResourceConverter<ClassPathResource> converter;

    DefaultClassPathResourcesService() {
        this.resourcesByLocation = new ConcurrentHashMap<>();
        this.converter = new DelegateClassPathResourceConverter();
    }

    @Override
    public void write(IndexedClassPathLocation targetLocation, ClassPathResources resources) {
        if (resources.isEmpty()) {
            return;
        }

        final var targetPath = Path.of(targetLocation.getLocation());

        final var dictionary = new AutoFillingDictionary(new MapDictionary(new HashMap<>()));

        final var resourcesOutputData =
                resources
                        .stream()
                        .map(resource -> this.converter.toString(resource, dictionary))
                        .collect(Collectors.joining(System.lineSeparator()));

        final var result =
                Dictionary.toString(dictionary, System.lineSeparator())
                        + DICTIONARY_SEPARATOR
                        + System.lineSeparator()
                        + resourcesOutputData;

        try {
            Files.writeString(
                    targetPath,
                    result,
                    StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE
            );
        } catch (IOException ex) {
            throw new IndexedClassPathStorageException("Unable to write indexed class path resources to config file: " + targetPath, ex);
        }
    }

    @Override
    public ClassPathResources read(IndexedClassPathLocation sourceLocation, Predicate<ClassPathResource> filter) {
        final ClassPathResources allResources = this.resourcesByLocation.computeIfAbsent(sourceLocation.getLocation(), k -> this.readAll(sourceLocation));
        return allResources
                .stream()
                .filter(filter)
                .collect(Collectors.toCollection(IndexedClassPathResources::new));
    }

    private ClassPathResources readAll(final IndexedClassPathLocation location) {
        final var result = new IndexedClassPathResources();

        final var configsUrls = readConfigsFromClassPath(location);
        for (final var resourceUrl : configsUrls) {
            try (final var configStream = resourceUrl.openStream();
                 final var configReader = new BufferedReader(new InputStreamReader(configStream, StandardCharsets.UTF_8))) {

                final Set<String> dictionaryMappings = new HashSet<>();

                var line = configReader.readLine();
                while (line != null && !line.equals(DICTIONARY_SEPARATOR)) {
                    dictionaryMappings.add(line);
                    line = configReader.readLine();
                }

                final var sourceDictionary = new MapDictionary(new HashMap<>());
                sourceDictionary.fill(dictionaryMappings);

                final var reversedDictionary = sourceDictionary.reverseDictionary();

                while (line != null) {
                    this.converter.fromString(line, reversedDictionary).ifPresent(result::add);
                    line = configReader.readLine();
                }
            } catch (IOException e) {
                throw new IndexedClassPathStorageException("Invalid config file provided: " + resourceUrl, e);
            }
        }

        return result;
    }

    private Collection<URL> readConfigsFromClassPath(final IndexedClassPathLocation location) {
        try {
            final Map<String, URL> result = new HashMap<>();
            for (final var cl : location.getTargetClassLoaders()) {
                Collections.list(cl.getResources(location.getLocation()))
                            .forEach(r -> result.putIfAbsent(r.getFile(), r));
            }

            return result.values();
        } catch (IOException e) {
            throw new IndexedClassPathStorageException("Unable to take indexed files", e);
        }
    }
}
