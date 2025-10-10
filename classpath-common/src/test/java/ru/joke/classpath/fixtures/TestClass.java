package ru.joke.classpath.fixtures;

import java.io.Serializable;

public class TestClass {

    public static class StaticNested extends TestClass {

        @SuppressWarnings("unused")
        public static final class Inner extends StaticNested implements Serializable, TestInterface {
            @TestAnnotation2
            @SuppressWarnings("unused")
            private final transient String var = "v1";

            public Inner() {}

            protected Inner(String v1, String v2, int v3, String[] v4, int... v5) {
            }

            private String getVar(String v1, int v2, StaticNested v3, String[] v4, int... v5) {
                return var;
            }
        }
    }
}
