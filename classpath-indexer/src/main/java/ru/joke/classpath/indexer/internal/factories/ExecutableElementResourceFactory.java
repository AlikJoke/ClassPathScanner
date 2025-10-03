package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassMethodResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
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
        return new ClassMethodResource() {

            @Override
            public List<ClassReference<?>> parameters() {
                return source.getParameters()
                                .stream()
                                .map(VariableElement::asType)
                                .filter(t -> t instanceof DeclaredType)
                                .map(t -> (DeclaredType) t)
                                .map(DeclaredType::asElement)
                                .filter(t -> t instanceof QualifiedNameable)
                                .map(t -> (QualifiedNameable) t)
                                .map(QualifiedNameable::getQualifiedName)
                                .map(Object::toString)
                                .map(ExecutableElementResourceFactory.this::createClassRef)
                                .collect(Collectors.toList());
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
                return Objects.hash(id());
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Executable f && f.id().equals(id());
            }

            @Override
            public String toString() {
                return type().name() + ":" + id();
            }

            @Override
            public ClassReference<?> owner() {
                if (source.getEnclosingElement() instanceof QualifiedNameable n) {
                    return createClassRef(n.getQualifiedName().toString());
                }

                throw new IllegalStateException();
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
