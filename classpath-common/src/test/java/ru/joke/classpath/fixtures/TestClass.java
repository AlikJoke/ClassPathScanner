package ru.joke.classpath.fixtures;

public class TestClass {

    public static class StaticNested {

        public static class Inner {
            @TestAnnotation2
            @SuppressWarnings("unused")
            private final transient String var = "v1";
        }
    }
}
