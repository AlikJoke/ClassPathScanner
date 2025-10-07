package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassFieldResource;
import ru.joke.classpath.IndexedClassPathException;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.VariableElement;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

final class FieldResourceFactory extends ClassPathResourceFactory<ClassFieldResource, VariableElement> {

    FieldResourceFactory(final ClassPathIndexingContext indexingContext) {
        super(indexingContext);
    }

    @Override
    public ClassFieldResource doCreate(VariableElement source) {
        final var ownerRef =
                source.getEnclosingElement() instanceof QualifiedNameable n
                        ? createClassRef(n)
                        : null;
        if (ownerRef == null) {
            throw new IndexedClassPathException("Unsupported type of field owner: " + source.getEnclosingElement());
        }

        return new ClassFieldResource() {
            @Override
            public Field asField(ClassLoader loader) {
                throw new UnsupportedOperationException();
            }

            @Override
            public ClassReference<?> owner() {
                return ownerRef;
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
                return obj instanceof ClassFieldResource f && Objects.equals(f.id(), id());
            }

            @Override
            public String toString() {
                return toStringDescription();
            }
        };
    }

    @Override
    protected Set<ElementKind> supportedTypes() {
        return Set.of(ElementKind.FIELD, ElementKind.ENUM_CONSTANT);
    }
}
