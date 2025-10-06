package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.converters.ClassPathResourceConverter;
import ru.joke.classpath.converters.Dictionary;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbsClassPathResourceConverter<T extends ClassPathResource> implements ClassPathResourceConverter<T> {

    protected static final String BLOCK_SEPARATOR = "|";
    protected static final String ELEMENTS_IN_BLOCK_DELIMITER = ";";
    protected static final String MEMBER_OF_CLASS_SEPARATOR = "#";

    private final int componentsCount;

    protected AbsClassPathResourceConverter(final int componentsCount) {
        this.componentsCount = componentsCount;
    }

    @Override
    public String toString(
            final T resource,
            final ru.joke.classpath.converters.Dictionary dictionary
    ) {

        final StringBuilder sb = new StringBuilder();

        sb.append(resource.type().alias()).append(BLOCK_SEPARATOR);

        appendModifiers(resource.modifiers(), sb).append(BLOCK_SEPARATOR);

        final var moduleName = dictionary.map(resource.module());
        sb.append(moduleName).append(BLOCK_SEPARATOR);

        final var packageName = dictionary.map(resource.packageName());
        sb.append(packageName).append(BLOCK_SEPARATOR);

        final var name = getResourceName(resource, dictionary);
        sb.append(name).append(BLOCK_SEPARATOR);

        appendAliases(resource.aliases(), sb).append(BLOCK_SEPARATOR);

        append(resource.annotations(), dictionary, sb);
        appendExtendedInfo(resource, dictionary, sb);

        return sb.toString();
    }

    @Override
    public Optional<T> fromString(
            final String resource,
            final ru.joke.classpath.converters.Dictionary dictionary
    ) {
        final var parts = resource.split("\\" + BLOCK_SEPARATOR, this.componentsCount);
        if (parts.length < this.componentsCount) {
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

    protected void appendExtendedInfo(T resource, ru.joke.classpath.converters.Dictionary dictionary, StringBuilder sb) {
    }

    protected T from(
            final Set<ClassPathResource.Modifier> modifiers,
            final String module,
            final String packageName,
            final String name,
            final Set<String> aliases,
            final Set<ClassPathResource.ClassReference<?>> annotations,
            final String[] parts,
            final ru.joke.classpath.converters.Dictionary dictionary
    ) {
        throw new UnsupportedOperationException();
    }

    protected String getResourceName(
            final String resourceNameStr,
            final ru.joke.classpath.converters.Dictionary dictionary
    ) {
        return dictionary.map(resourceNameStr);
    }

    protected String getResourceName(
            final T resource,
            final ru.joke.classpath.converters.Dictionary dictionary
    ) {
        return dictionary.map(resource.name());
    }

    protected final Set<ClassPathResource.ClassReference<?>> extractRefs(
            final String classesStr,
            final ru.joke.classpath.converters.Dictionary dictionary
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

    protected final ClassPathResource.ClassReference<?> createClassRef(final String id) {
        return new ClassReferenceImpl<>(id);
    }

    protected final StringBuilder append(
            final Set<ClassPathResource.ClassReference<?>> classes,
            final ru.joke.classpath.converters.Dictionary dictionary,
            final StringBuilder builder
    ) {
        classes.forEach(
                c -> builder.append(dictionary.map(c.canonicalName()))
                            .append(ELEMENTS_IN_BLOCK_DELIMITER)
        );

        return builder;
    }

    protected final Set<ClassPathResource.Modifier> extractModifiers(final String strModifiers) {
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

    protected final StringBuilder appendModifiers(
            final Set<ClassPathResource.Modifier> modifiers,
            final StringBuilder builder
    ) {
        modifiers.forEach(
                m -> builder.append(m.alias()).append(ELEMENTS_IN_BLOCK_DELIMITER)
        );

        return builder;
    }

    protected final StringBuilder appendAliases(
            final Set<String> aliases,
            final StringBuilder builder
    ) {
        aliases.forEach(
                a -> builder.append(a).append(ELEMENTS_IN_BLOCK_DELIMITER)
        );

        return builder;
    }

    protected final Set<String> extractAliases(
            final String aliasesStr,
            final Dictionary dictionary
    ) {
        if (aliasesStr.isBlank()) {
            return Collections.emptySet();
        }

        return Set.of(aliasesStr.split(ELEMENTS_IN_BLOCK_DELIMITER))
                    .stream()
                    .filter(s -> !s.isEmpty())
                    .map(dictionary::map)
                    .collect(Collectors.toUnmodifiableSet());
    }

    protected final String toStringDescription(final T resource) {
        return resource.type() + "@" + resource.id();
    }
}
