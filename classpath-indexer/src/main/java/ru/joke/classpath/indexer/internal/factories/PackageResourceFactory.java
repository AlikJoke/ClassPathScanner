package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.PackageResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

final class PackageResourceFactory extends ClassPathResourceFactory<PackageResource, PackageElement> {

    PackageResourceFactory(final ClassPathIndexingContext indexingContext) {
        super(indexingContext);
    }

    @Override
    public PackageResource doCreate(PackageElement source) {
        final var name = source.getQualifiedName().toString();
        final var aliases = findAliases(source, name);

        final var modifiers = mapModifiers(source.getModifiers());

        final Set<ClassPathResource.ClassReference<?>> annotations = new HashSet<>();
        collectAnnotations(source, annotations);

        return new PackageResource() {
            @Override
            public Optional<Package> asPackage(ClassLoader loader) {
                throw new UnsupportedOperationException();
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
                return indexingContext.moduleName();
            }

            @Override
            public Set<ClassReference<?>> annotations() {
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
                return obj instanceof PackageResource f && Objects.equals(f.id(), id());
            }

            @Override
            public String toString() {
                return toStringDescription();
            }
        };
    }

    @Override
    protected Set<ElementKind> supportedTypes() {
        return Set.of(ElementKind.PACKAGE);
    }
}
