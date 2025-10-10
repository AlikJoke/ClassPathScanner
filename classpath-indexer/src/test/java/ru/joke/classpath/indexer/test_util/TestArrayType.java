package ru.joke.classpath.indexer.test_util;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

final class TestArrayType extends TestTypeMirror implements ArrayType {

    private final TypeMirror componentType;

    public TestArrayType(Class<?> type) {
        super(TypeKind.ARRAY, type);
        this.componentType = Util.toTypeMirror(type.getComponentType());
    }

    @Override
    public TypeMirror getComponentType() {
        return this.componentType;
    }
}
