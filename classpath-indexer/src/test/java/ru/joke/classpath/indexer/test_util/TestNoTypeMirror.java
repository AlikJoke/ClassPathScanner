package ru.joke.classpath.indexer.test_util;

import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import java.lang.reflect.AnnotatedElement;

final class TestNoTypeMirror extends TestTypeMirror implements NoType {

    TestNoTypeMirror(TypeKind kind, AnnotatedElement type) {
        super(kind, type);
    }
}
