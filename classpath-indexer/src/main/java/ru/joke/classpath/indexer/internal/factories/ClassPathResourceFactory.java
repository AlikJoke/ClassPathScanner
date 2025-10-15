package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassPathIndexed;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.internal.configs.ClassPathIndexingConfiguration;
import ru.joke.classpath.indexer.internal.configs.ScannedResources;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.stream.Collectors;

import static ru.joke.classpath.ClassPathResource.ClassReference.BINARY_NESTED_ID_SEPARATOR;
import static ru.joke.classpath.ClassPathResource.ClassReference.CANONICAL_NAME_SEPARATOR;

/**
 * An abstract classpath resource factory that provides API to create a {@link ClassPathResource} based
 * on a program element of type {@link Element}.<br>
 *
 * @author Alik
 * @see ClassPathResource
 * @param <T> concrete type of the classpath resource
 * @param <E> concrete type of the program element
 */
public abstract class ClassPathResourceFactory<T extends ClassPathResource, E extends Element> {

    protected final ClassPathIndexingContext indexingContext;

    protected ClassPathResourceFactory(final ClassPathIndexingContext indexingContext) {
        this.indexingContext = indexingContext;
    }

    /**
     * Creates the classpath resource based on the provided program element.
     *
     * @param source provided program element; cannot be {@code null}.
     * @return cannot be {@code null}.
     */
    public Optional<T> create(E source) {
        return Optional.of(doCreate(source));
    }

    protected abstract T doCreate(E source);

    protected abstract Set<ElementKind> supportedTypes();

    protected String findPackageName(final Element source) {
        var enclosedElement = source.getEnclosingElement();

        if (enclosedElement instanceof ExecutableElement) {
            final var sourceType = source.asType();
            return sourceType instanceof DeclaredType d
                    ? findPackageName(d.asElement())
                    : "";
        }

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
                .filter(Objects::nonNull)
                .filter(a -> a.asElement() != null)
                .map(a -> createClassRef(findPackageName(a.asElement()), findQualifiedName(a)))
                .filter(annotations::add)
                .map(ClassPathResource.ClassReference::canonicalName)
                .map(this.indexingContext.elementUtils()::getTypeElement)
                .filter(Objects::nonNull)
                .forEach(a -> collectAnnotations(a, annotations));
    }

    protected String findQualifiedName(final TypeMirror mirror) {
        return mirror instanceof DeclaredType type && type.asElement() instanceof QualifiedNameable q
                ? q.getQualifiedName().toString()
                : mirror instanceof PrimitiveType p
                    ? p.getKind().name().toLowerCase()
                    : mirror instanceof ArrayType a
                        ? getArrayBinaryName(a)
                        : "";
    }

    private String getArrayBinaryName(final ArrayType type) {
        final var componentType = type.getComponentType();
        if (componentType.getKind().isPrimitive()) {
            return "[" + componentType.getKind().name().charAt(0);
        }

        return "[" + findQualifiedName(componentType) + ';';
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
            result.addAll(Arrays.asList(annotation.value()));
        }
        this.indexingContext.prevScannedResources()
                            .stream()
                            .map(ScannedResources::aliases)
                            .filter(aliases -> aliases.containsKey(elementId))
                            .map(aliases -> aliases.getOrDefault(elementId, Collections.emptySet()))
                            .forEach(result::addAll);

        return result;
    }

    protected ClassPathResource.ClassReference<?> createClassRef(final Element element) {
        final var packageName = findPackageName(element);
        final var canonicalName = findQualifiedName(element.asType());

        return createClassRef(packageName, canonicalName);
    }

    protected ClassPathResource.ClassReference<?> createClassRef(
            final String packageName,
            final String canonicalName
    ) {
        if (canonicalName == null) {
            return null;
        }

        final var binaryName =
                packageName == null || packageName.isEmpty()
                        ? canonicalName.replace(CANONICAL_NAME_SEPARATOR, BINARY_NESTED_ID_SEPARATOR)
                        : packageName + CANONICAL_NAME_SEPARATOR + canonicalName.substring(packageName.length() + 1).replace(CANONICAL_NAME_SEPARATOR, BINARY_NESTED_ID_SEPARATOR);

        return new ClassPathResource.ClassReference<>() {
            @Override
            public String canonicalName() {
                return canonicalName;
            }

            @Override
            public String binaryName() {
                return binaryName;
            }

            @Override
            public Class<Object> toClass(ClassLoader loader) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int hashCode() {
                return binaryName.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof ClassPathResource.ClassReference<?> ref)) {
                    return false;
                }

                return binaryName.equals(ref.binaryName());
            }
        };
    }


    protected Set<ClassPathResource.Modifier> mapModifiers(final Set<Modifier> modifiers) {
        if (modifiers.isEmpty()) {
            return EnumSet.noneOf(ClassPathResource.Modifier.class);
        }

        final var result = modifiers
                            .stream()
                            .map(this::mapModifier)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
        return result.isEmpty()
                ? EnumSet.noneOf(ClassPathResource.Modifier.class)
                : EnumSet.copyOf(result);
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
