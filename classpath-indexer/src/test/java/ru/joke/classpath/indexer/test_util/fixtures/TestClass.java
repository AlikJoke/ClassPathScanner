package ru.joke.classpath.indexer.test_util.fixtures;

import ru.joke.classpath.ClassPathIndexed;

import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.annotation.Documented;

@SuppressWarnings("unused")
@ClassPathIndexed("top_c")
public abstract class TestClass {

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

    @ClassPathIndexed("nested_c")
    public static class NestedClass extends TestClass {

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

    public interface Interface extends Externalizable, Cloneable {
        @Deprecated
        Class<?> ref = Interface.class;

        private static void staticInterfaceMethod(NestedClass v1, int v2, String v3) {
        }

        @Deprecated
        default Class<?> getRef() {
            return ref;
        }

        abstract class Test extends NestedClass implements Interface {

        }

        @Documented
        @interface TestAnnotation {

        }
    }

    public record Record(@Deprecated @ClassPathIndexed("int") int intField) implements Interface {

        @Deprecated
        @ClassPathIndexed("record_c")
        public Record {
            if (intField < 0) {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void writeExternal(ObjectOutput out) {

        }

        @Override
        public void readExternal(ObjectInput in) {

        }
    }
}
