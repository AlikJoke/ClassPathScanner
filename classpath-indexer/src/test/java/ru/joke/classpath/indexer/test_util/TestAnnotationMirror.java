package ru.joke.classpath.indexer.test_util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import java.util.Map;

final class TestAnnotationMirror implements AnnotationMirror {

    private final Class<?> annotationType;

    TestAnnotationMirror(Class<?> annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    public DeclaredType getAnnotationType() {
        return new TestClassType(annotationType);
    }

    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
        throw new UnsupportedOperationException();
    }
}
