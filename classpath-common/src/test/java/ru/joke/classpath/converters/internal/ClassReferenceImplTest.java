package ru.joke.classpath.converters.internal;

import org.junit.jupiter.api.Test;
import ru.joke.classpath.fixtures.TestAnnotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassReferenceImplTest {

    @Test
    void testPrimitiveType() throws ClassNotFoundException {
        final var ref1 = new ClassReferenceImpl<>(int.class.getCanonicalName());
        assertEquals(int.class.getCanonicalName(), ref1.canonicalName());
        assertEquals(int.class, ref1.toClass());

        final var ref2 = new ClassReferenceImpl<>("int");
        assertEquals(ref1, ref2);
        assertEquals(ref1.toClass(), ref2.toClass());
    }

    @Test
    void testUnknownType() {
        final var ref = new ClassReferenceImpl<>("ru.joke.TestUnknown");
        assertThrows(ClassNotFoundException.class, ref::toClass);
    }

    @Test
    void testObjectType() throws ClassNotFoundException {
        final var ref = new ClassReferenceImpl<>("ru.joke.classpath.fixtures.TestAnnotation");
        assertEquals(TestAnnotation.class.getCanonicalName(), ref.canonicalName());
        assertEquals(TestAnnotation.class, ref.toClass());
    }
}
