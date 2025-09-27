package ru.joke.classpath.converters;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ConcreteClassPathResourceConverter;

import java.util.Optional;
import java.util.Set;

public final class ClassResourceConverter extends AbsClassPathResourceConverter<ClassPathResource.ClassResource<?>> implements ConcreteClassPathResourceConverter<ClassPathResource.ClassResource<?>> {

    @Override
    public String toString(ClassPathResource.ClassResource<?> resource) {
        return super.toString(resource)
                + BLOCK_SEPARATOR
                + transform(resource.interfaces())
                + BLOCK_SEPARATOR
                + transform(resource.superClasses());
    }

    @Override
    public Optional<ClassPathResource.ClassResource<?>> fromString(String resource) {
        final var parts = resource.split("\\" + BLOCK_SEPARATOR);
        if (parts.length < 8) {
            return Optional.empty();
        }

        final var module = parts[1];
        final var packageName = parts[2];
        final var name = parts[3];

        final var aliases = Set.of(parts[4].split(ELEMENTS_IN_BLOCK_DELIMITER));
        final var annotations = extractRefs(parts[5]);

        final var interfaces = extractRefs(parts[6]);
        final var superClasses = extractRefs(parts[7]);

        return Optional.of(new ClassPathResource.ClassResource<>() {

            private volatile Class<Object> clazz;

            @Override
            public synchronized Class<Object> asClass() throws ClassNotFoundException {
                if (this.clazz == null) {
                    return this.clazz = new ClassReferenceImpl<>(id()).toClass();
                }

                return this.clazz;
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
        });
    }

    @Override
    public ClassPathResource.Type supportedType() {
        return ClassPathResource.Type.CLASS;
    }
}
