package ru.joke.classpath.indexer.test_util;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public final class TestVariableElement extends TestElement<Field> implements VariableElement {

    public TestVariableElement(Field source) {
        super(
                source.getType().isPrimitive() ? new TestPrimitiveType(source.getType()) : new TestClassType(source.getType()),
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
        return source.isEnumConstant() ? ElementKind.ENUM_CONSTANT : ElementKind.FIELD;
    }

    @Override
    public Element getEnclosingElement() {
        return new TestTypeElement(this.source.getDeclaringClass());
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return Collections.emptyList();
    }
}
