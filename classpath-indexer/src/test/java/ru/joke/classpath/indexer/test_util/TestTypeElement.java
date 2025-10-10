package ru.joke.classpath.indexer.test_util;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.stream.Collectors;

public final class TestTypeElement extends TestElement<Class<?>> implements TypeElement {

    public TestTypeElement(final Class<?> type) {
        super(
                new TestClassType(type),
                collectModifiers(type),
                type.getSimpleName(),
                type
        );
    }

    private static Set<Modifier> collectModifiers(final Class<?> type) {
        final Set<Modifier> modifiers = new HashSet<>();
        if (type.isSealed()) {
            modifiers.add(Modifier.SEALED);
        }

        modifiers.addAll(Util.collectModifiers(type.getModifiers()));

        return modifiers.isEmpty()
                ? EnumSet.noneOf(Modifier.class)
                : EnumSet.copyOf(modifiers);
    }

    @Override
    public ElementKind getKind() {
        return this.source.isAnnotation()
                ? ElementKind.ANNOTATION_TYPE
                : this.source.isInterface()
                    ? ElementKind.INTERFACE
                    : this.source.isEnum()
                        ? ElementKind.ENUM
                        : this.source.isRecord()
                            ? ElementKind.RECORD
                            : ElementKind.CLASS;
    }

    @Override
    public Element getEnclosingElement() {
        return this.source.getEnclosingClass() != null
                ? new TestTypeElement(this.source.getEnclosingClass())
                : new TestPackageElement(
                        this.source.getPackage(),
                        new TestModuleElement(getClass().getModule())
                    );
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return Collections.emptyList();
    }

    @Override
    public NestingKind getNestingKind() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Name getQualifiedName() {
        return new TestName(this.source.getCanonicalName());
    }

    @Override
    public TypeMirror getSuperclass() {
        return new TestClassType(this.source.getSuperclass());
    }

    @Override
    public List<? extends TypeMirror> getInterfaces() {
        return Arrays.stream(this.source.getInterfaces())
                        .map(TestClassType::new)
                        .collect(Collectors.toList());
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters() {
        throw new UnsupportedOperationException();
    }
}
