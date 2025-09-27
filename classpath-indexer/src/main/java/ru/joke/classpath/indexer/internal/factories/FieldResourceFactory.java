package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.VariableElement;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

final class FieldResourceFactory extends ClassPathResourceFactory<ClassPathResource.FieldResource, VariableElement> {

    FieldResourceFactory(final ClassPathIndexingContext indexingContext) {
        super(indexingContext);
    }

    @Override
    public ClassPathResource.FieldResource doCreate(VariableElement source) {
        return new ClassPathResource.FieldResource() {
            @Override
            public Field asField() {
                throw new UnsupportedOperationException();
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
        return Set.of(ElementKind.FIELD);
    }
}
