package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

final class ClassResourceFactory extends ClassPathResourceFactory<ClassPathResource.ClassResource<?>, TypeElement> {

    ClassResourceFactory(final ClassPathIndexingContext context) {
        super(context);
    }

    @Override
    public ClassPathResource.ClassResource<?> doCreate(TypeElement source) {
        return new ClassPathResource.ClassResource<>() {
            @Override
            public Class<Object> asClass() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<ClassReference<?>> interfaces() {
                final Set<ClassReference<?>> interfaces = new HashSet<>();
                collectInterfaces(interfaces, source);

                final Set<TypeElement> superclasses = new HashSet<>();
                collectSuperclasses(superclasses, source);
                superclasses.forEach(s -> collectInterfaces(interfaces, s));

                return interfaces;
            }

            @Override
            public Set<ClassReference<?>> superClasses() {
                final Set<TypeElement> superclasses = new HashSet<>();
                collectSuperclasses(superclasses, source);

                return superclasses
                        .stream()
                        .map(s -> createClassRef(s.getQualifiedName().toString()))
                        .collect(Collectors.toSet());
            }

            @Override
            public String name() {
                return source.getQualifiedName().toString();
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

            private void collectInterfaces(final Set<ClassReference<?>> interfaces, final TypeElement type) {
                type.getInterfaces()
                        .stream()
                        .filter(i -> i instanceof DeclaredType iMirror && iMirror.asElement() instanceof QualifiedNameable)
                        .map(i -> (QualifiedNameable) i)
                        .map(QualifiedNameable::getQualifiedName)
                        .map(Object::toString)
                        .peek(i -> interfaces.add(createClassRef(i)))
                        .map(indexingContext.elementUtils()::getTypeElement)
                        .filter(Objects::nonNull)
                        .forEach(i -> this.collectInterfaces(interfaces, i));
            }

            private void collectSuperclasses(final Set<TypeElement> superClasses, final TypeElement type) {
                final var superClass = type.getSuperclass();
                if (superClass instanceof DeclaredType superClassMirror
                        && superClassMirror.asElement() instanceof QualifiedNameable nameable) {
                    final var superClassName = nameable.getQualifiedName().toString();
                    if (superClassName.equals(Object.class.getCanonicalName())) {
                        return;
                    }

                    final var superClassType = indexingContext.elementUtils().getTypeElement(superClassName);
                    if (superClassType == null) {
                        return;
                    }

                    superClasses.add(superClassType);
                    collectSuperclasses(superClasses, superClassType);
                }
            }
        };
    }

    @Override
    protected Set<ElementKind> supportedTypes() {
        return Set.of(ElementKind.CLASS, ElementKind.INTERFACE, ElementKind.ENUM, ElementKind.RECORD, ElementKind.ANNOTATION_TYPE);
    }
}
