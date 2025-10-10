package ru.joke.classpath.indexer.test_util.fixtures;

import ru.joke.classpath.ClassPathIndexed;

@SuppressWarnings("unused")
public class TestClass {

    @ClassPathIndexed("int_f")
    private volatile int intField;

    @ClassPathIndexed("int_m")
    final int getIntField() {
        return intField;
    }

    @ClassPathIndexed("int_c")
    TestClass(int intField) {
        this.intField = intField;
    }

    @ClassPathIndexed("int_ca")
    TestClass(int... intField) {
        this.intField = intField[0];
    }

    native void nativeMethod();

    public static class NestedClass {

        @ClassPathIndexed("string_f")
        private static final String stringField = "";

        @ClassPathIndexed("string_m")
        static String getStringField() {
            return stringField;
        }

        @ClassPathIndexed("nested_c")
        public NestedClass() {
        }

        public NestedClass(int[] v) {
        }
    }

    public enum Enum {
        E1 {
            @Override
            @ClassPathIndexed("e1_m")
            synchronized void enumMethod(Enum v) {
                super.enumMethod(v);
            }
        },
        E2;

        Enum() {}

        synchronized void enumMethod(Enum v) {
        }
    }

    public interface Interface {
        @Deprecated
        Class<?> ref = Interface.class;

        private static void staticInterfaceMethod(NestedClass v1, int v2, String v3) {
        }

        @Deprecated
        default Class<?> getRef() {
            return ref;
        }
    }

    public record Record(@Deprecated @ClassPathIndexed("int") int intField) {

        @Deprecated
        @ClassPathIndexed("record_c")
        public Record {
            if (intField < 0) {
                throw new IllegalArgumentException();
            }
        }
    }
}
