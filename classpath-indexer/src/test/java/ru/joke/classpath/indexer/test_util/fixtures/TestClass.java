package ru.joke.classpath.indexer.test_util.fixtures;

import ru.joke.classpath.ClassPathIndexed;

@SuppressWarnings("unused")
public class TestClass {

    @ClassPathIndexed("int")
    private volatile int intField;

    public static class NestedClass {

        @ClassPathIndexed("string")
        private static final String stringField = "";
    }

    public enum Enum {
        E1,
        E2
    }

    public interface Interface {
        @Deprecated
        Class<?> ref = Interface.class;
    }

    public record Record(@Deprecated @ClassPathIndexed("int") int intField) {
    }
}
