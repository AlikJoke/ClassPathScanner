package ru.joke.classpath.indexer.test_util;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

abstract class TestElement<T> implements Element {

    protected final TypeMirror typeMirror;
    protected final Set<Modifier> modifiers;
    protected final Name simpleName;
    protected final T source;

    protected TestElement(
            final TypeMirror typeMirror,
            final Set<Modifier> modifiers,
            final String simpleName,
            final T source
    ) {
        this.typeMirror = typeMirror;
        this.modifiers = modifiers;
        this.simpleName = new TestName(simpleName);
        this.source = source;
    }

    @Override
    public TypeMirror asType() {
        return typeMirror;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    @Override
    public Name getSimpleName() {
        return this.simpleName;
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        if (!(this.source instanceof AnnotatedElement ae)) {
            return Collections.emptyList();
        }

        return Arrays.stream(ae.getDeclaredAnnotations())
                        .map(a -> new TestAnnotationMirror(a.annotationType()))
                        .collect(Collectors.toList());
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        if (!(this.source instanceof AnnotatedElement ae)) {
            return null;
        }

        return ae.getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        if (!(this.source instanceof AnnotatedElement ae)) {
            @SuppressWarnings("unchecked")
            final A[] result = (A[]) new Annotation[0];
            return result;
        }

        return ae.getAnnotationsByType(annotationType);
    }

    @Override
    public int hashCode() {
        return this.source.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        return ((TestElement<?>) obj).source.equals(source);
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        throw new UnsupportedOperationException();
    }
}
