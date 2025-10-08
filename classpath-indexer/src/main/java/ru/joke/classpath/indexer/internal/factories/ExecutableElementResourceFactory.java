package ru.joke.classpath.indexer.internal.factories;

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

final class ExecutableElementResourceFactory extends ClassPathResourceFactory<ClassMethodResource, ExecutableElement> {

    ExecutableElementResourceFactory(final ClassPathIndexingContext indexingContext) {
        super(indexingContext);
    }

    @Override
    public ClassMethodResource doCreate(ExecutableElement source) {
        final List<ClassPathResource.ClassReference<?>> parameters =
                source.getParameters()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(ExecutableElementResourceFactory.this::createClassRef)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        final var parametersStr = parameters
                                        .stream()
                                        .map(ClassPathResource.ClassReference::binaryName)
                                        .collect(Collectors.joining(";"));
        final var ownerRef =
                source.getEnclosingElement() instanceof QualifiedNameable n
                        ? createClassRef(n)
                        : null;
        if (ownerRef == null) {
            throw new IndexedClassPathException("Unsupported type of method owner: " + source.getEnclosingElement());
        }

        final var module = indexingContext.moduleName();
        final var methodName = source.getSimpleName().toString();
        final var methodId = (module.isEmpty() ? "" : module.concat("/")) + ownerRef.binaryName()
                + ClassMethodResource.ID_SEPARATOR + methodName + "(" + parametersStr + ")";

        final var modifiers = mapModifiers(source.getModifiers());
        final var aliases = findAliases(source, methodId);
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
            public String id() {
                return methodId;
            }

            @Override
            public String name() {
                return methodName;
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
