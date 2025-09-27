package ru.joke.classpath.converters;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ConcreteClassPathResourceConverter;

import java.util.Optional;
import java.util.Set;

public final class ModuleResourceConverter extends AbsClassPathResourceConverter<ClassPathResource.ModuleResource> implements ConcreteClassPathResourceConverter<ClassPathResource.ModuleResource> {

    @Override
    public Optional<ClassPathResource.ModuleResource> fromString(String resource) {
        final var parts = resource.split("\\" + BLOCK_SEPARATOR);
        if (parts.length < 6) {
            return Optional.empty();
        }

        final var module = parts[1];

        final var aliases = Set.of(parts[4].split(ELEMENTS_IN_BLOCK_DELIMITER));
        final var annotations = extractRefs(parts[5]);

        return Optional.of(new ClassPathResource.ModuleResource() {

            @Override
            public Optional<Module> asModule() {
                return getClass().getModule().getLayer().findModule(module);
            }

            @Override
            public String name() {
                return module;
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
                return "";
            }
        });
    }

    @Override
    public ClassPathResource.Type supportedType() {
        return ClassPathResource.Type.MODULE;
    }
}
