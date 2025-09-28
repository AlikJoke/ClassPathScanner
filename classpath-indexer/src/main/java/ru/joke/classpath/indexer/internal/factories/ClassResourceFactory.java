package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassFieldResource;
import ru.joke.classpath.ClassResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

final class ClassResourceFactory extends ClassPathResourceFactory<ClassResource<?>, TypeElement> {

    ClassResourceFactory(final ClassPathIndexingContext context) {
        super(context);
    }

    @Override
    public ClassResource<?> doCreate(TypeElement source) {
        return new ClassResource<>() {
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
            public Kind kind() {
                return switch (source.getKind()) {
                    case CLASS -> Kind.CLASS;
                    case ENUM -> Kind.ENUM;
                    case RECORD -> Kind.RECORD;
                    case INTERFACE -> Kind.INTERFACE;
                    case ANNOTATION_TYPE -> Kind.ANNOTATION;
                    default -> throw new UnsupportedOperationException();
                };
            }

            @Override
            public String name() {
                var name = source.getSimpleName().toString();
                var enclosedElement = source.getEnclosingElement();
                while (enclosedElement != null && enclosedElement.getKind() != ElementKind.PACKAGE) {
                    name = enclosedElement.getSimpleName() + ID_SEPARATOR + name;
                    enclosedElement = enclosedElement.getEnclosingElement();
                }

                return name;
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
                return Objects.hash(id());
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof ClassFieldResource f && f.id().equals(id());
            }

            @Override
            public String toString() {
                return type().name() + ":" + id();
            }

            private void collectInterfaces(final Set<ClassReference<?>> interfaces, final TypeElement type) {
                type.getInterfaces()
                        .stream()
                        .filter(i -> i instanceof DeclaredType)
                        .map(i -> ((DeclaredType) i).asElement())
                        .filter(i -> i instanceof QualifiedNameable)
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
