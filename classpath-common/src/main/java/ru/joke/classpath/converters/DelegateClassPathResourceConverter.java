package ru.joke.classpath.converters;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.converters.internal.*;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DelegateClassPathResourceConverter extends AbsClassPathResourceConverter<ClassPathResource> {

    private static final Map<ClassPathResource.Type, ConcreteClassPathResourceConverter<ClassPathResource>> converters = findConverters();

    public DelegateClassPathResourceConverter() {
        super(Integer.MAX_VALUE);
    }

    @Override
    public Optional<ClassPathResource> fromString(
            final String resource,
            final Dictionary dictionary
    ) {
        return detectType(resource)
                .map(converters::get)
                .flatMap(c -> c.fromString(resource, dictionary));
    }

    @Override
    public String toString(
            final ClassPathResource resource,
            final Dictionary dictionary
    ) {
        final var converter = converters.get(resource.type());
        return converter.toString(resource, dictionary);
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
        final var result =
                Stream.of(
                        (ConcreteClassPathResourceConverter<? extends ClassPathResource>) new ClassResourceConverter(),
                        new ModuleResourceConverter(),
                        new PackageResourceConverter(),
                        new ClassFieldResourceConverter(),
                        new ClassMethodResourceConverter(),
                        new ClassConstructorResourceConverter()
                )
                .map(c -> (ConcreteClassPathResourceConverter<ClassPathResource>) c)
                .collect(Collectors.toMap(ConcreteClassPathResourceConverter::supportedType, Function.identity()));
        return result;
    }
}
