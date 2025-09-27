package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class ExecutableElementResourceFactory extends ClassPathResourceFactory<ClassPathResource.MethodResource, ExecutableElement> {

    ExecutableElementResourceFactory(final ClassPathIndexingContext indexingContext) {
        super(indexingContext);
    }

    @Override
    public ClassPathResource.MethodResource doCreate(ExecutableElement source) {
        return new ClassPathResource.MethodResource() {

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
            public Method asMethod() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Type type() {
                return name().equals("<cinit>") ? Type.CONSTRUCTOR : Type.METHOD;
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
