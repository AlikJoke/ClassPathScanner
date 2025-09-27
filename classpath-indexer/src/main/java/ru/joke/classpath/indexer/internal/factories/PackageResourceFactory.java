package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.PackageResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

final class PackageResourceFactory extends ClassPathResourceFactory<PackageResource, PackageElement> {

    PackageResourceFactory(final ClassPathIndexingContext indexingContext) {
        super(indexingContext);
    }

    @Override
    public PackageResource doCreate(PackageElement source) {
        return new PackageResource() {
            @Override
            public Optional<Package> asPackage() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String name() {
                return source.getQualifiedName().toString();
            }

            @Override
            public Set<String> aliases() {
                return findAliases(source, name());
            }

            @Override
            public String module() {
                return name();
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                final Set<ClassReference<?>> annotations = new HashSet<>();
                collectAnnotations(source, annotations);

                return annotations;
            }

            @Override
            public Set<Modifier> modifiers() {
                return mapModifiers(source.getModifiers());
            }
        };
    }

    @Override
    protected Set<ElementKind> supportedTypes() {
        return Set.of(ElementKind.PACKAGE);
    }
}
