package ru.joke.classpath.indexer.test_util;

import javax.lang.model.element.Name;

final class TestName implements Name {

    private final String name;

    TestName(String name) {
        this.name = name;
    }

    @Override
    public boolean contentEquals(CharSequence cs) {
        return this.name.contentEquals(cs);
    }

    @Override
    public int length() {
        return this.name.length();
    }

    @Override
    public char charAt(int index) {
        return this.name.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.name.subSequence(start, end);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
