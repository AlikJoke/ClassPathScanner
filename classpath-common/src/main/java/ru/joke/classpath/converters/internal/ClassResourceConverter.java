package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassResource;
import ru.joke.classpath.converters.Dictionary;

import java.util.Objects;
import java.util.Set;

import static ru.joke.classpath.ClassResource.ID_SEPARATOR;

public final class ClassResourceConverter extends AbsClassPathResourceConverter<ClassResource<?>> implements ConcreteClassPathResourceConverter<ClassResource<?>> {

    private static final int COMPONENTS_COUNT = 10;

    public ClassResourceConverter() {
        super(COMPONENTS_COUNT);
    }

    @Override
    protected void appendExtendedInfo(
            final ClassResource<?> resource,
            final Dictionary dictionary,
            final StringBuilder builder
    ) {
        builder.append(BLOCK_SEPARATOR);
        append(resource.interfaces(), dictionary, builder).append(BLOCK_SEPARATOR);
        append(resource.superClasses(), dictionary, builder).append(BLOCK_SEPARATOR);
        builder.append(resource.kind().alias());
    }

    @Override
    protected ClassResource<?> from(
            final Set<ClassPathResource.Modifier> modifiers,
            final String module,
            final String packageName,
            final String name,
            final Set<String> aliases,
            final Set<ClassPathResource.ClassReference<?>> annotations,
            final String[] parts,
            final Dictionary dictionary
    ) {
        final var interfaces = extractRefs(parts[7], dictionary);
        final var superClasses = extractRefs(parts[8], dictionary);
        final var kind = ClassResource.Kind.from(parts[9]);

        final var classRef = new ClassReferenceImpl<>(packageName + ID_SEPARATOR + name);

        return new ClassResource<>() {

            @Override
            public Class<Object> asClass(ClassLoader loader) throws ClassNotFoundException {
                return classRef.toClass(loader);
            }

            @Override
            public Set<ClassReference<?>> interfaces() {
                return interfaces;
            }

            @Override
            public Set<ClassReference<?>> superClasses() {
                return superClasses;
            }

            @Override
            public Kind kind() {
                return kind;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public Set<String> aliases() {
                return aliases;
            }

            @Override
            public String module() {
                return module;
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                return annotations;
            }

            @Override
            public String packageName() {
                return packageName;
            }

            @Override
            public Set<Modifier> modifiers() {
                return modifiers;
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(id());
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof ClassResource<?> f && Objects.equals(f.id(), id());
            }

            @Override
            public String toString() {
                return ClassResourceConverter.this.toStringDescription(this);
            }
        };
    }

    @Override
    public ClassPathResource.Type supportedType() {
        return ClassPathResource.Type.CLASS;
    }
}
