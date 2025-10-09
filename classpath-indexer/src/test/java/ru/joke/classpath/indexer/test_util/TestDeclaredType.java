package ru.joke.classpath.indexer.test_util;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public abstract class TestDeclaredType extends TestTypeMirror implements DeclaredType, TypeMirror {

    protected final Class<?> type;

    protected TestDeclaredType(Class<?> type) {
        super(TypeKind.DECLARED, type);
        this.type = type;
    }

    @Override
    public TypeMirror getEnclosingType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends TypeMirror> getTypeArguments() {
        throw new UnsupportedOperationException();
    }
}
