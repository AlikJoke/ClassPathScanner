package ru.joke.classpath.converters;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassPathResourceConverter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

abstract class AbsClassPathResourceConverter<T extends ClassPathResource> implements ClassPathResourceConverter<T> {

    protected static final String BLOCK_SEPARATOR = "|";
    protected static final String ELEMENTS_IN_BLOCK_DELIMITER = ";";
    protected static final String MEMBER_OF_CLASS_SEPARATOR = "#";

    @Override
    public String toString(T resource) {

        final String aliasesBlockStr =
                resource.aliases()
                        .stream()
                        .collect(Collectors.joining(ELEMENTS_IN_BLOCK_DELIMITER, BLOCK_SEPARATOR, BLOCK_SEPARATOR));

        return resource.type().alias()
                + BLOCK_SEPARATOR
                + resource.module()
                + BLOCK_SEPARATOR
                + resource.packageName()
                + BLOCK_SEPARATOR
                + getResourceName(resource)
                + BLOCK_SEPARATOR
                + aliasesBlockStr
                + BLOCK_SEPARATOR
                + transform(resource.annotations());
    }

    protected String getResourceName(T resource) {
        return resource.name();
    }

    protected Set<ClassPathResource.ClassReference<?>> extractRefs(final String classesStr) {
        final var result =
                Set.of(classesStr.split(ELEMENTS_IN_BLOCK_DELIMITER))
                        .stream()
                        .map(this::createClassRef)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        return Collections.unmodifiableSet(result);
    }

    protected ClassPathResource.ClassReference<?> createClassRef(final String id) {
        return new ClassReferenceImpl<>(id);
    }

    protected String transform(final Set<ClassPathResource.ClassReference<?>> classes) {
        return classes
                .stream()
                .map(ClassPathResource.ClassReference::canonicalName)
                .collect(Collectors.joining(ELEMENTS_IN_BLOCK_DELIMITER, BLOCK_SEPARATOR, BLOCK_SEPARATOR));
    }
}
