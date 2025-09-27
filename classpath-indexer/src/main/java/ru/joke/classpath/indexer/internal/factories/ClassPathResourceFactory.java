package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassPathIndexed;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.internal.ScannedResources;
import ru.joke.classpath.indexer.internal.config.ClassPathIndexingConfiguration;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.type.DeclaredType;
import java.util.*;

public abstract class ClassPathResourceFactory<T extends ClassPathResource, E extends Element> {

    protected final ClassPathIndexingContext indexingContext;

    protected ClassPathResourceFactory(final ClassPathIndexingContext indexingContext) {
        this.indexingContext = indexingContext;
    }

    public Optional<T> create(E source) {
        return Optional.of(doCreate(source));
    }

    protected abstract T doCreate(E source);

    protected abstract Set<ElementKind> supportedTypes();

    protected String findPackageName(final Element source) {
        var enclosedElement = source.getEnclosingElement();
        while (enclosedElement != null && enclosedElement.getKind() != ElementKind.PACKAGE) {
            enclosedElement = enclosedElement.getEnclosingElement();
        }

        return enclosedElement instanceof QualifiedNameable q ? q.getQualifiedName().toString() : "";
    }

    protected void collectAnnotations(final Element element, final Set<ClassPathResource.ClassReference<?>> annotations) {
        element.getAnnotationMirrors()
                .stream()
                .map(AnnotationMirror::getAnnotationType)
                .map(DeclaredType::asElement)
                .filter(a -> a instanceof QualifiedNameable)
                .map(a -> (QualifiedNameable) a)
                .map(QualifiedNameable::getQualifiedName)
                .map(Object::toString)
                .peek(a -> annotations.add(createClassRef(a)))
                .map(this.indexingContext.elementUtils()::getTypeElement)
                .filter(Objects::nonNull)
                .forEach(a -> collectAnnotations(a, annotations));
    }

    protected Set<String> findAliases(final Element element, final String elementId) {
        final Set<String> result = new HashSet<>(
                this.indexingContext.indexingConfiguration()
                                    .map(ClassPathIndexingConfiguration::aliases)
                                    .map(aliases -> aliases.get(elementId))
                                    .orElse(Collections.emptySet())
        );
        final var annotation = element.getAnnotation(ClassPathIndexed.class);
        if (annotation != null) {
            result.addAll(Arrays.asList(annotation.aliases()));
        }
        this.indexingContext.prevScannedResources()
                        .stream()
                        .map(ScannedResources::aliases)
                        .filter(aliases -> aliases.containsKey(elementId))
                        .map(aliases -> aliases.getOrDefault(elementId, Collections.emptySet()))
                        .forEach(result::addAll);

        return result;
    }

    protected ClassPathResource.ClassReference<?> createClassRef(final String className) {
        return new ClassPathResource.ClassReference<>() {
            @Override
            public String canonicalName() {
                return className;
            }

            @Override
            public Class<Object> toClass() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int hashCode() {
                return className.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof ClassPathResource.ClassReference<?> ref)) {
                    return false;
                }

                return className.equals(ref.canonicalName());
            }
        };
    }
}
