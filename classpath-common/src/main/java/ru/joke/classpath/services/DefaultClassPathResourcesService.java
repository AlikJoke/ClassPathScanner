package ru.joke.classpath.services;

import ru.joke.classpath.*;
import ru.joke.classpath.converters.ClassPathResourceConverter;

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

public final class DefaultClassPathResourcesService implements ClassPathResourcesService {

    private final Map<String, ClassPathResources> resourcesByLocation;
    private final ClassPathResourceConverter<ClassPathResource> converter;

    public DefaultClassPathResourcesService() {
        this.resourcesByLocation = new ConcurrentHashMap<>();
        this.converter = findConverter();
    }

    @Override
    public void write(IndexedClassPathLocation targetLocation, ClassPathResources resources) {
        if (resources.isEmpty()) {
            return;
        }

        final var targetPath = Path.of(targetLocation.getLocation());

        final var outputData =
                resources
                        .stream()
                        .map(this.converter::toString)
                        .collect(Collectors.joining(System.lineSeparator()));

        try {
            Files.writeString(
                    targetPath,
                    outputData,
                    StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE
            );
        } catch (IOException ex) {
            throw new IndexedClassPathStorageException("Unable to write indexed class path resources to config file: " + targetPath, ex);
        }
    }

    @Override
    public ClassPathResources read(IndexedClassPathLocation sourceLocation, Predicate<ClassPathResource> filter) {
        final ClassPathResources allResources = this.resourcesByLocation.computeIfAbsent(sourceLocation.getLocation(), this::readAll);
        return allResources
                .stream()
                .filter(filter)
                .collect(Collectors.toCollection(IndexedClassPathResources::new));
    }

    @SuppressWarnings("unchecked")
    private ClassPathResourceConverter<ClassPathResource> findConverter() {
        return ServiceLoader.load(ClassPathResourceConverter.class, ClassPathResourceConverter.class.getClassLoader()).findFirst().orElseThrow();
    }

    private ClassPathResources readAll(final String sourceLocation) {
        final var result = new IndexedClassPathResources();

        final var configsUrls = readConfigsFromClassPath(sourceLocation);
        while (configsUrls.hasMoreElements()) {
            final var resourceUrl = configsUrls.nextElement();
            try (final var configStream = resourceUrl.openStream();
                 final var configReader = new BufferedReader(new InputStreamReader(configStream, StandardCharsets.UTF_8))) {

                var line = configReader.readLine();
                while (line != null) {
                    this.converter.fromString(line).ifPresent(result::add);
                    line = configReader.readLine();
                }
            } catch (IOException e) {
                throw new IndexedClassPathStorageException("Invalid config file provided: " + resourceUrl, e);
            }
        }

        return result;
    }

    private Enumeration<URL> readConfigsFromClassPath(final String resourcePath) {
        try {
            return getClass().getClassLoader().getResources(resourcePath);
        } catch (IOException e) {
            throw new IndexedClassPathStorageException("Unable to take indexed files", e);
        }
    }
}
