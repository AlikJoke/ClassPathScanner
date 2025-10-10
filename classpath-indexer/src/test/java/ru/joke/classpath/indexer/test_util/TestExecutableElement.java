package ru.joke.classpath.indexer.test_util;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public final class TestExecutableElement extends TestElement<Executable> implements ExecutableElement {

    public TestExecutableElement(final Executable source) {
        super(
                Util.toTypeMirror(source instanceof Method m ? m.getReturnType() : void.class),
                collectModifiers(source),
                source.getName(),
                source
        );
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters() {
        return Collections.emptyList();
    }

    @Override
    public TypeMirror getReturnType() {
        return asType();
    }

    @Override
    public List<? extends VariableElement> getParameters() {
        return Arrays.stream(this.source.getParameters())
                        .map(TestParameterElement::new)
                        .collect(Collectors.toList());
    }

    @Override
    public TypeMirror getReceiverType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isVarArgs() {
        return this.source.isVarArgs();
    }

    @Override
    public boolean isDefault() {
        return this.source instanceof Method m && m.isDefault();
    }

    @Override
    public List<? extends TypeMirror> getThrownTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AnnotationValue getDefaultValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ElementKind getKind() {
        return this.source instanceof Method ? ElementKind.METHOD : ElementKind.CONSTRUCTOR;
    }

    @Override
    public Element getEnclosingElement() {
        return new TestTypeElement(this.source.getDeclaringClass());
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return Collections.emptyList();
    }

    private static Set<Modifier> collectModifiers(final Executable source) {
        final var result = new HashSet<>(Util.collectModifiers(source.getModifiers()));
        if (source instanceof Method m && m.isDefault()) {
            result.add(Modifier.DEFAULT);
        }

        return result;
    }

    private static class TestParameterElement extends TestElement<Parameter> implements VariableElement {

        private TestParameterElement(final Parameter source) {
            super(
                    Util.toTypeMirror(source.getType()),
                    Util.collectModifiers(source.getModifiers()),
                    source.getName(),
                    source
            );
        }

        @Override
        public Object getConstantValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FIELD;
        }

        @Override
        public Element getEnclosingElement() {
            return new TestExecutableElement(this.source.getDeclaringExecutable());
        }

        @Override
        public List<? extends Element> getEnclosedElements() {
            return Collections.emptyList();
        }
    }
}
