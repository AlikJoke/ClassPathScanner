package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.PackageResource;
import ru.joke.classpath.converters.Dictionary;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

final class PackageResourceConverter extends AbsClassPathResourceConverter<PackageResource> implements ConcreteClassPathResourceConverter<PackageResource> {

    private static final int COMPONENTS_COUNT = 7;

    PackageResourceConverter() {
        super(COMPONENTS_COUNT);
    }

    @Override
    public ClassPathResource.Type supportedType() {
        return ClassPathResource.Type.PACKAGE;
    }

    @Override
    protected PackageResource from(
            final Set<ClassPathResource.Modifier> modifiers,
            final String module,
            final String packageName,
            final String name,
            final Set<String> aliases,
            final Set<ClassPathResource.ClassReference<?>> annotations,
            final String[] parts,
            final Dictionary dictionary
    ) {
        return new PackageResource() {
            @Override
            public Optional<Package> asPackage(ClassLoader loader) {
                return Optional.ofNullable(loader.getDefinedPackage(name));
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
                return obj == this || obj instanceof PackageResource p && Objects.equals(p.id(), id());
            }

            @Override
            public String toString() {
                return toStringDescription();
            }
        };
    }
}
