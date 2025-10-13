package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ModuleResource;
import ru.joke.classpath.converters.Dictionary;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

final class ModuleResourceConverter extends AbsClassPathResourceConverter<ModuleResource> implements ConcreteClassPathResourceConverter<ModuleResource> {

    private static final int COMPONENTS_COUNT = 7;

    ModuleResourceConverter() {
        super(COMPONENTS_COUNT);
    }

    @Override
    public ClassPathResource.Type supportedType() {
        return ClassPathResource.Type.MODULE;
    }

    @Override
    protected ModuleResource from(
            final Set<ClassPathResource.Modifier> modifiers,
            final String module,
            final String packageName,
            final String name,
            final Set<String> aliases,
            final Set<ClassPathResource.ClassReference<?>> annotations,
            final String[] parts,
            final Dictionary dictionary
    ) {
        return new ModuleResource() {

            @Override
            public Optional<Module> asModule(ModuleLayer layer) {
                return layer.findModule(module);
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
            public Set<ClassPathResource.ClassReference<?>> annotations() {
                return annotations;
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
                return obj == this || obj instanceof ModuleResource m && Objects.equals(m.id(), id());
            }

            @Override
            public String toString() {
                return toStringDescription();
            }
        };
    }
}
