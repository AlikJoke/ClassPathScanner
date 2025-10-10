package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassMemberResource;
import ru.joke.classpath.ClassMethodResource;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.IndexedClassPathException;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.QualifiedNameable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

final class ClassExecutableElementResourceFactory extends ClassPathResourceFactory<ClassMemberResource.Executable, ExecutableElement> {

    private static final String CONSTRUCTOR_NAME = "<cinit>";

    ClassExecutableElementResourceFactory(final ClassPathIndexingContext indexingContext) {
        super(indexingContext);
    }

    @Override
    public ClassMethodResource doCreate(ExecutableElement source) {
        final List<ClassPathResource.ClassReference<?>> parameters =
                source.getParameters()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(ClassExecutableElementResourceFactory.this::createClassRef)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        final var ownerRef =
                source.getEnclosingElement() instanceof QualifiedNameable n
                        ? createClassRef(n)
                        : null;
        if (ownerRef == null) {
            throw new IndexedClassPathException("Unsupported type of method owner: " + source.getEnclosingElement());
        }

        final var module = indexingContext.moduleName();
        final var methodName = source.getKind() == ElementKind.CONSTRUCTOR
                ? CONSTRUCTOR_NAME
                : source.getSimpleName().toString();

        final var modifiers = mapModifiers(source.getModifiers());
        final var packageName = findPackageName(source);

        final Set<ClassPathResource.ClassReference<?>> annotations = new HashSet<>();
        collectAnnotations(source, annotations);

        return new ClassMethodResource() {

            @Override
            public List<ClassReference<?>> parameters() {
                return parameters;
            }

            @Override
            public Method asMethod(ClassLoader loader) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Type type() {
                return source.getKind() == ElementKind.CONSTRUCTOR ? Type.CONSTRUCTOR : Type.METHOD;
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
                return obj instanceof Executable f && Objects.equals(f.id(), id());
            }

            @Override
            public String toString() {
                return toStringDescription();
            }

            @Override
            public ClassReference<?> owner() {
                return ownerRef;
            }

            @Override
            public String name() {
                return methodName;
            }

            @Override
            public Set<String> aliases() {
                return findAliases(source, id());
            }

            @Override
            public String module() {
                return module;
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                return Collections.unmodifiableSet(annotations);
            }

            @Override
            public String packageName() {
                return packageName;
            }
        };
    }

    @Override
    protected Set<ElementKind> supportedTypes() {
        return Set.of(ElementKind.METHOD, ElementKind.CONSTRUCTOR);
    }
}
