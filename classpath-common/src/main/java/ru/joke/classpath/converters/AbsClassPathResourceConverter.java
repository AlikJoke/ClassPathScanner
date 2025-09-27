package ru.joke.classpath.converters;

import ru.joke.classpath.ClassPathResource;

import java.util.*;
import java.util.stream.Collectors;

abstract class AbsClassPathResourceConverter<T extends ClassPathResource> implements ClassPathResourceConverter<T> {

    protected static final String BLOCK_SEPARATOR = "|";
    protected static final String ELEMENTS_IN_BLOCK_DELIMITER = ";";
    protected static final String MEMBER_OF_CLASS_SEPARATOR = "#";

    private final int componentsCount;

    protected AbsClassPathResourceConverter(int componentsCount) {
        this.componentsCount = componentsCount;
    }

    @Override
    public String toString(T resource) {

        final var resourceType = resource.type().alias();
        final var aliasesStr = String.join(ELEMENTS_IN_BLOCK_DELIMITER, resource.aliases());
        final var modifiersStr = transformModifiers(resource.modifiers());
        final var name = getResourceName(resource);
        final var annotationsStr = transform(resource.annotations());

        return resourceType
                + BLOCK_SEPARATOR
                + modifiersStr
                + BLOCK_SEPARATOR
                + resource.module()
                + BLOCK_SEPARATOR
                + resource.packageName()
                + BLOCK_SEPARATOR
                + name
                + BLOCK_SEPARATOR
                + aliasesStr
                + BLOCK_SEPARATOR
                + annotationsStr;
    }

    @Override
    public Optional<T> fromString(String resource) {
        final var parts = resource.split("\\" + BLOCK_SEPARATOR, this.componentsCount);
        if (parts.length < this.componentsCount || parts.length < 7) {
            return Optional.empty();
        }

        final var modifiers = extractModifiers(parts[1]);
        final var module = parts[2];
        final var packageName = parts[3];
        final var name = parts[4];
        final var aliases = extractAliases(parts[5]);
        final var annotations = extractRefs(parts[6]);

        return Optional.of(from(modifiers, module, packageName, name, aliases, annotations, parts));
    }

    protected T from(
            final Set<ClassPathResource.Modifier> modifiers,
            final String module,
            final String packageName,
            final String name,
            final Set<String> aliases,
            final Set<ClassPathResource.ClassReference<?>> annotations,
            final String[] parts
    ) {
        throw new UnsupportedOperationException();
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
                .collect(Collectors.joining(ELEMENTS_IN_BLOCK_DELIMITER));
    }

    protected Set<ClassPathResource.Modifier> extractModifiers(final String strModifiers) {
        final var modifiers =
                Arrays.stream(strModifiers.split(ELEMENTS_IN_BLOCK_DELIMITER))
                        .filter(m -> !m.isBlank())
                        .map(ClassPathResource.Modifier::from)
                        .collect(Collectors.toSet());
        return EnumSet.copyOf(modifiers);
    }

    protected String transformModifiers(final Set<ClassPathResource.Modifier> modifiers) {
        return modifiers
                .stream()
                .map(ClassPathResource.Modifier::alias)
                .collect(Collectors.joining(ELEMENTS_IN_BLOCK_DELIMITER));
    }

    protected Set<String> extractAliases(final String aliasesStr) {
        return Set.of(aliasesStr.split(ELEMENTS_IN_BLOCK_DELIMITER));
    }
}
