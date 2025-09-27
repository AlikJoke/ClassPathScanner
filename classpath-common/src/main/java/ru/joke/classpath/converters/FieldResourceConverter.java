package ru.joke.classpath.converters;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ConcreteClassPathResourceConverter;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

public final class FieldResourceConverter extends AbsClassPathResourceConverter<ClassPathResource.FieldResource> implements ConcreteClassPathResourceConverter<ClassPathResource.FieldResource> {

    @Override
    public Optional<ClassPathResource.FieldResource> fromString(String resource) {
        final var parts = resource.split("\\" + BLOCK_SEPARATOR);
        if (parts.length < 6) {
            return Optional.empty();
        }

        final var module = parts[1];
        final var packageName = parts[2];

        final var nameParts = parts[3].split(MEMBER_OF_CLASS_SEPARATOR);
        final var className = nameParts[0];
        final var fieldName = nameParts[1];

        final var aliases = Set.of(parts[4].split(ELEMENTS_IN_BLOCK_DELIMITER));
        final var annotations = extractRefs(parts[5]);

        final var owner = new ClassReferenceImpl<>(packageName + "." + className);

        return Optional.of(new ClassPathResource.FieldResource() {

            @Override
            public Field asField() throws NoSuchFieldException, ClassNotFoundException {
                return owner().toClass().getDeclaredField(fieldName);
            }

            @Override
            public ClassReference<?> owner() {
                return owner;
            }

            @Override
            public String name() {
                return fieldName;
            }

            @Override
            public String id() {
                return parts[3];
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
        return ClassPathResource.Type.FIELD;
    }

    @Override
    protected String getResourceName(ClassPathResource.FieldResource resource) {
        return resource.owner().canonicalName().substring(resource.packageName().length() + 1)
                + MEMBER_OF_CLASS_SEPARATOR
                + resource.name();
    }
}
