package ru.joke.classpath.converters;

import ru.joke.classpath.ClassPathResource;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DelegateClassPathResourceConverter extends AbsClassPathResourceConverter<ClassPathResource> {

    private final Map<ClassPathResource.Type, ConcreteClassPathResourceConverter<ClassPathResource>> converters;

    public DelegateClassPathResourceConverter() {
        this(findConverters());
    }

    private DelegateClassPathResourceConverter(Map<ClassPathResource.Type, ConcreteClassPathResourceConverter<ClassPathResource>> converters) {
        super(Integer.MAX_VALUE);
        this.converters = new HashMap<>(converters);
    }

    @Override
    public Optional<ClassPathResource> fromString(String resource) {
        return detectType(resource)
                .map(this.converters::get)
                .flatMap(c -> c.fromString(resource));
    }

    @Override
    public String toString(ClassPathResource resource) {
        return this.converters.get(resource.type()).toString(resource);
    }

    private Optional<ClassPathResource.Type> detectType(final String resource) {
        final int typeSeparatorIdx = resource.indexOf(BLOCK_SEPARATOR);
        if (typeSeparatorIdx == -1) {
            return Optional.empty();
        }

        final var type = ClassPathResource.Type.from(resource.substring(0, typeSeparatorIdx));
        return Optional.ofNullable(type);
    }

    private static Map<ClassPathResource.Type, ConcreteClassPathResourceConverter<ClassPathResource>> findConverters() {
        @SuppressWarnings("unchecked")
        final var converters =
                ServiceLoader.load(ConcreteClassPathResourceConverter.class, ConcreteClassPathResourceConverter.class.getClassLoader())
                                .stream()
                                .map(ServiceLoader.Provider::get)
                                .map(c -> (ConcreteClassPathResourceConverter<ClassPathResource>) c)
                                .collect(Collectors.toMap(ConcreteClassPathResourceConverter::supportedType, Function.identity()));
        return converters;
    }
}
