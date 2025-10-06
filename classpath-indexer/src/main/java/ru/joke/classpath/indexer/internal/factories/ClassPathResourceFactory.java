package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassPathIndexed;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.internal.configs.ScannedResources;
import ru.joke.classpath.indexer.internal.configs.ClassPathIndexingConfiguration;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.stream.Collectors;

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

    protected void collectAnnotations(
            final Element element,
            final Set<ClassPathResource.ClassReference<?>> annotations
    ) {
        element.getAnnotationMirrors()
                .stream()
                .map(AnnotationMirror::getAnnotationType)
                .map(this::findQualifiedName)
                .filter(Objects::nonNull)
                .filter(a -> annotations.add(createClassRef(a)))
                .map(this.indexingContext.elementUtils()::getTypeElement)
                .filter(Objects::nonNull)
                .forEach(a -> collectAnnotations(a, annotations));
    }

    protected String findQualifiedName(final TypeMirror mirror) {
        return mirror instanceof DeclaredType type && type.asElement() instanceof QualifiedNameable q
                ? q.getQualifiedName().toString()
                : null;
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
            public Class<Object> toClass(ClassLoader loader) {
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


    protected Set<ClassPathResource.Modifier> mapModifiers(final Set<Modifier> modifiers) {
        final var result = modifiers
                            .stream()
                            .map(this::mapModifier)
                            .collect(Collectors.toSet());
        return EnumSet.copyOf(result);
    }

    private ClassPathResource.Modifier mapModifier(final Modifier modifier) {
        return switch (modifier) {
            case PUBLIC -> ClassPathResource.Modifier.PUBLIC;
            case PROTECTED -> ClassPathResource.Modifier.PROTECTED;
            case PRIVATE -> ClassPathResource.Modifier.PRIVATE;
            case ABSTRACT -> ClassPathResource.Modifier.ABSTRACT;
            case DEFAULT -> ClassPathResource.Modifier.DEFAULT;
            case STATIC -> ClassPathResource.Modifier.STATIC;
            case SEALED -> ClassPathResource.Modifier.SEALED;
            case NON_SEALED -> ClassPathResource.Modifier.NON_SEALED;
            case FINAL -> ClassPathResource.Modifier.FINAL;
            case TRANSIENT -> ClassPathResource.Modifier.TRANSIENT;
            case VOLATILE -> ClassPathResource.Modifier.VOLATILE;
            case SYNCHRONIZED -> ClassPathResource.Modifier.SYNCHRONIZED;
            case NATIVE -> ClassPathResource.Modifier.NATIVE;
            default -> null;
        };
    }
}
