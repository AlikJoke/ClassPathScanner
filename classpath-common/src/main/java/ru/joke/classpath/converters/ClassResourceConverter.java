package ru.joke.classpath.converters;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassResource;

import java.util.Set;

import static ru.joke.classpath.ClassResource.ID_SEPARATOR;

public final class ClassResourceConverter extends AbsClassPathResourceConverter<ClassResource<?>> implements ConcreteClassPathResourceConverter<ClassResource<?>> {

    private static final int COMPONENTS_COUNT = 10;

    public ClassResourceConverter() {
        super(COMPONENTS_COUNT);
    }

    @Override
    public String toString(ClassResource<?> resource) {
        return super.toString(resource)
                + BLOCK_SEPARATOR
                + transform(resource.interfaces())
                + BLOCK_SEPARATOR
                + transform(resource.superClasses())
                + BLOCK_SEPARATOR
                + resource.kind().alias();
    }

    @Override
    protected ClassResource<?> from(
            final Set<ClassPathResource.Modifier> modifiers,
            final String module,
            final String packageName,
            final String name,
            final Set<String> aliases,
            final Set<ClassPathResource.ClassReference<?>> annotations,
            final String[] parts
    ) {
        final var interfaces = extractRefs(parts[7]);
        final var superClasses = extractRefs(parts[8]);
        final var kind = ClassResource.Kind.from(parts[9]);

        final var classRef = new ClassReferenceImpl<>(packageName + ID_SEPARATOR + name);

        return new ClassResource<>() {

            @Override
            public Class<Object> asClass() throws ClassNotFoundException {
                return classRef.toClass();
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
        };
    }

    @Override
    public ClassPathResource.Type supportedType() {
        return ClassPathResource.Type.CLASS;
    }
}
