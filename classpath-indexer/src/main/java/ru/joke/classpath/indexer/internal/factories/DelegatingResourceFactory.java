package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.*;

public final class DelegatingResourceFactory extends ClassPathResourceFactory<ClassPathResource, Element> {

    private final Map<ElementKind, ClassPathResourceFactory<? extends ClassPathResource, Element>> factories;

    public DelegatingResourceFactory(final ClassPathIndexingContext indexingContext) {
        super(indexingContext);

        final Map<ElementKind, ClassPathResourceFactory<? extends ClassPathResource, Element>> factories = new HashMap<>();
        createFactories(indexingContext)
                .forEach(
                        factory -> factory.supportedTypes()
                                                .forEach(type -> factories.put(type, cast(factory)))
                );

        this.factories = Map.copyOf(factories);
    }

    @Override
    public Optional<ClassPathResource> create(Element source) {
        return Optional.ofNullable(doCreate(source));
    }

    @Override
    public ClassPathResource doCreate(Element source) {
        final var factory = this.factories.get(source.getKind());
        if (factory == null) {
            return null;
        }

        return factory.doCreate(source);
    }

    @Override
    protected Set<ElementKind> supportedTypes() {
        return Set.copyOf(this.factories.keySet());
    }

    private ClassPathResourceFactory<? extends ClassPathResource, Element> cast(ClassPathResourceFactory<? extends ClassPathResource, ? extends Element> factory) {
        @SuppressWarnings("unchecked")
        final var result = (ClassPathResourceFactory<? extends ClassPathResource, Element>) factory;
        return result;
    }

    private List<ClassPathResourceFactory<? extends ClassPathResource, ? extends Element>> createFactories(
            final ClassPathIndexingContext indexingContext
    ) {
        return List.of(
                new ClassResourceFactory(indexingContext),
                new ModuleResourceFactory(indexingContext),
                new ExecutableElementResourceFactory(indexingContext),
                new ClassFieldResourceFactory(indexingContext),
                new PackageResourceFactory(indexingContext)
        );
    }
}
