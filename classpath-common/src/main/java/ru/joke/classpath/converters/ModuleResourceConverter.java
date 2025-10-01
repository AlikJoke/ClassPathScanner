package ru.joke.classpath.converters;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ModuleResource;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class ModuleResourceConverter extends AbsClassPathResourceConverter<ModuleResource> implements ConcreteClassPathResourceConverter<ModuleResource> {

    private static final int COMPONENTS_COUNT = 7;

    public ModuleResourceConverter() {
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
            public Optional<Module> asModule() {
                final var callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
                return callerClass.getModule().getLayer().findModule(module);
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
            public Set<ClassPathResource.ClassReference<?>> annotations() {
                return annotations;
            }

            @Override
            public Set<Modifier> modifiers() {
                return modifiers;
            }

            @Override
            public int hashCode() {
                return Objects.hash(id());
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof ModuleResource f && f.id().equals(id());
            }

            @Override
            public String toString() {
                return type().name() + ":" + id();
            }
        };
    }
}
