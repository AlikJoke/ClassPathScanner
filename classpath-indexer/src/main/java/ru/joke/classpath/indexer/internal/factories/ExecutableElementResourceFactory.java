package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassMethodResource;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.IndexedClassPathException;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.VariableElement;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
                        .map(VariableElement::asType)
                        .map(ExecutableElementResourceFactory.this::findQualifiedName)
                        .filter(Objects::nonNull)
                        .map(ExecutableElementResourceFactory.this::createClassRef)
                        .collect(Collectors.toList());
        final var parametersStr = parameters
                                        .stream()
                                        .map(ClassPathResource.ClassReference::canonicalName)
                                        .collect(Collectors.joining(","));
        final var ownerRef =
                source.getEnclosingElement() instanceof QualifiedNameable n
                        ? createClassRef(n.getQualifiedName().toString())
                        : null;
        if (ownerRef == null) {
            throw new IndexedClassPathException("Unsupported type of method owner: " + source.getEnclosingElement());
        }

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
                return mapModifiers(source.getModifiers());
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
                return ownerRef.canonicalName() + ID_SEPARATOR + name() + "(" + parametersStr + ")";
            }

            @Override
            public String name() {
                return source.getSimpleName().toString();
            }

            @Override
            public Set<String> aliases() {
                return findAliases(source, id());
            }

            @Override
            public String module() {
                return indexingContext.moduleName();
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                final Set<ClassReference<?>> annotations = new HashSet<>();
                collectAnnotations(source, annotations);

                return annotations;
            }

            @Override
            public String packageName() {
                return findPackageName(source);
            }
        };
    }

    @Override
    protected Set<ElementKind> supportedTypes() {
        return Set.of(ElementKind.METHOD, ElementKind.CONSTRUCTOR);
    }
}
