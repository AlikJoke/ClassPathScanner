package ru.joke.classpath.indexer.test_util;

import javax.lang.model.element.Element;

final class TestClassType extends TestDeclaredType {

    TestClassType(Class<?> type) {
        super(type);
    }

    @Override
    public Element asElement() {
        return new TestTypeElement(type);
    }
}
