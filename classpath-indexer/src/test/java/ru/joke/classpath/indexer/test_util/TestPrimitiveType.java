package ru.joke.classpath.indexer.test_util;

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;

final class TestPrimitiveType extends TestTypeMirror implements PrimitiveType {

    TestPrimitiveType(Class<?> type) {
        super(toKind(type), type);
    }

    static TypeKind toKind(Class<?> type) {
        if (type == int.class) {
            return TypeKind.INT;
        } else if (type == double.class) {
            return TypeKind.DOUBLE;
        } else if (type == float.class) {
            return TypeKind.FLOAT;
        } else if (type == long.class) {
            return TypeKind.LONG;
        } else if (type == boolean.class) {
            return TypeKind.BOOLEAN;
        } else if (type == char.class) {
            return TypeKind.CHAR;
        } else if (type == void.class) {
            return TypeKind.VOID;
        } else if (type == short.class) {
            return TypeKind.SHORT;
        } else {
            throw new UnsupportedOperationException(type.getCanonicalName());
        }
    }
}
