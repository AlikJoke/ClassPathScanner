package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.*;

/**
 * A classpath resource factory that creates a {@link ClassPathResource} based on a program element of type {@link Element}.<br>
 * This implementation creates the resource by delegating the call to a specific factory capable of handling
 * the given type of program element.
 *
 * @author Alik
 * @see ClassPathResourceFactory
 * @see ClassPathResource
 */
public final class DelegatingResourceFactory extends ClassPathResourceFactory<ClassPathResource, Element> {

    private final Map<ElementKind, ClassPathResourceFactory<? extends ClassPathResource, Element>> factories;

    DelegatingResourceFactory(
            final ClassPathIndexingContext indexingContext,
            final List<ClassPathResourceFactory<? extends ClassPathResource, ? extends Element>> delegateFactories
    ) {
        super(indexingContext);
        final Map<ElementKind, ClassPathResourceFactory<? extends ClassPathResource, Element>> factories = new HashMap<>();
        delegateFactories.forEach(
                factory -> factory.supportedTypes()
                                        .forEach(type -> factories.put(type, cast(factory)))
        );

        this.factories = Map.copyOf(factories);
    }

    /**
     * Constructs the factory with specified indexing context.
     *
     * @param indexingContext indexing context; cannot be {@code null}.
     * @see ClassPathIndexingContext
     */
    public DelegatingResourceFactory(final ClassPathIndexingContext indexingContext) {
        this(indexingContext, createFactories(indexingContext));
    }

    @Override
    public Optional<ClassPathResource> create(Element source) {
        return Optional.ofNullable(doCreate(source));
    }

    @Override
    protected ClassPathResource doCreate(Element source) {
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

    private static List<ClassPathResourceFactory<? extends ClassPathResource, ? extends Element>> createFactories(
            final ClassPathIndexingContext indexingContext
    ) {
        return List.of(
                new ClassResourceFactory(indexingContext),
                new ModuleResourceFactory(indexingContext),
                new ClassExecutableElementResourceFactory(indexingContext),
                new ClassFieldResourceFactory(indexingContext),
                new PackageResourceFactory(indexingContext)
        );
    }
}
