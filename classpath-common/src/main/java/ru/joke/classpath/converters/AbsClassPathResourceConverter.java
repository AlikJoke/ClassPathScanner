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
    public String toString(T resource, Dictionary dictionary) {

        final var resourceType = resource.type().alias();
        final var aliasesStr =
                resource.aliases()
                        .stream()
                        .map(dictionary::map)
                        .collect(Collectors.joining(ELEMENTS_IN_BLOCK_DELIMITER));
        final var modifiersStr = transformModifiers(resource.modifiers());
        final var name = getResourceName(resource, dictionary);
        final var annotationsStr = transform(resource.annotations(), dictionary);

        return resourceType
                + BLOCK_SEPARATOR
                + modifiersStr
                + BLOCK_SEPARATOR
                + dictionary.map(resource.module())
                + BLOCK_SEPARATOR
                + dictionary.map(resource.packageName())
                + BLOCK_SEPARATOR
                + name
                + BLOCK_SEPARATOR
                + aliasesStr
                + BLOCK_SEPARATOR
                + annotationsStr;
    }

    @Override
    public Optional<T> fromString(String resource, Dictionary dictionary) {
        final var parts = resource.split("\\" + BLOCK_SEPARATOR, this.componentsCount);
        if (parts.length < this.componentsCount || parts.length < 7) {
            return Optional.empty();
        }

        final var modifiers = extractModifiers(parts[1]);
        final var module = dictionary.map(parts[2]);
        final var packageName = dictionary.map(parts[3]);
        final var name = getResourceName(parts[4], dictionary);
        final var aliases = extractAliases(parts[5], dictionary);
        final var annotations = extractRefs(parts[6], dictionary);

        return Optional.of(from(modifiers, module, packageName, name, aliases, annotations, parts, dictionary));
    }

    protected T from(
            final Set<ClassPathResource.Modifier> modifiers,
            final String module,
            final String packageName,
            final String name,
            final Set<String> aliases,
            final Set<ClassPathResource.ClassReference<?>> annotations,
            final String[] parts,
            final Dictionary dictionary
    ) {
        throw new UnsupportedOperationException();
    }

    protected String getResourceName(String resourceNameStr, Dictionary dictionary) {
        return dictionary.map(resourceNameStr);
    }

    protected String getResourceName(T resource, Dictionary dictionary) {
        return dictionary.map(resource.name());
    }

    protected Set<ClassPathResource.ClassReference<?>> extractRefs(
            final String classesStr,
            final Dictionary dictionary
    ) {
        if (classesStr.isBlank()) {
            return Collections.emptySet();
        }

        final var result =
                Set.of(classesStr.split(ELEMENTS_IN_BLOCK_DELIMITER))
                        .stream()
                        .map(dictionary::map)
                        .map(this::createClassRef)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        return Collections.unmodifiableSet(result);
    }

    protected ClassPathResource.ClassReference<?> createClassRef(final String id) {
        return new ClassReferenceImpl<>(id);
    }

    protected String transform(
            final Set<ClassPathResource.ClassReference<?>> classes,
            final Dictionary dictionary
    ) {
        return classes
                .stream()
                .map(ClassPathResource.ClassReference::canonicalName)
                .map(dictionary::map)
                .collect(Collectors.joining(ELEMENTS_IN_BLOCK_DELIMITER));
    }

    protected Set<ClassPathResource.Modifier> extractModifiers(final String strModifiers) {
        if (strModifiers.isBlank()) {
            return Collections.emptySet();
        }

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

    protected Set<String> extractAliases(
            final String aliasesStr,
            final Dictionary dictionary
    ) {
        if (aliasesStr.isBlank()) {
            return Collections.emptySet();
        }

        return Set.of(aliasesStr.split(ELEMENTS_IN_BLOCK_DELIMITER))
                    .stream()
                    .map(dictionary::map)
                    .collect(Collectors.toUnmodifiableSet());
    }
}
