package ru.joke.classpath.indexer.test_util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestTypeMirror implements TypeMirror {

    private final TypeKind kind;
    private final AnnotatedElement type;

    public TestTypeMirror(TypeKind kind, AnnotatedElement type) {
        this.kind = kind;
        this.type = type;
    }

    @Override
    public TypeKind getKind() {
        return kind;
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        return Arrays.stream(this.type.getDeclaredAnnotations())
                        .map(a -> new TestAnnotationMirror(a.annotationType()))
                        .collect(Collectors.toList());
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return this.type.getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return this.type.getAnnotationsByType(annotationType);
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        throw new UnsupportedOperationException();
    }
}
