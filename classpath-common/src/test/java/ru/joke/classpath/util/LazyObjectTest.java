package ru.joke.classpath.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LazyObjectTest {

    @Test
    void testObjectLoadedOnlyOne() throws Exception {

        final var lazyObject = new LazyObject<>() {
            @Override
            protected Object load(Object c) {
                return new Object();
            }
        };

        final var firstCall = lazyObject.get(null);
        final var secondCall = lazyObject.get(null);
        final var thirdCall = lazyObject.get(null);

        assertSame(firstCall, secondCall);
        assertSame(secondCall, thirdCall);
    }

    @Test
    void testWhenExceptionRaisedThenPropagatedToCaller() {
        var expectedException = new Exception("Load failed");

        LazyObject<Object, Object> lazyObject = new LazyObject<>() {
            @Override
            protected Object load(Object c) throws Exception {
                throw expectedException;
            }
        };

        final var actualException = assertThrows(Exception.class, () -> lazyObject.get(null));
        assertSame(expectedException, actualException);
    }

    @Test
    void testLoadObjectOnlyOnceWhenCalledFromMultipleThreads() throws Exception {
        final var lazyObject = new LazyObject<>() {
            @Override
            protected Object load(Object c) {
                return new Object();
            }
        };

        final var numberOfThreads = 10;
        final var threads = new Thread[numberOfThreads];
        final var results = new Object[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            int index = i;
            threads[i] = new Thread(() -> {
                try {
                    results[index] = lazyObject.get(null);
                } catch (Exception e) {
                    fail("Exception during get()", e);
                }
            });
            threads[i].start();
        }

        for (var thread : threads) {
            thread.join();
        }

        for (var result : results) {
            assertSame(results[0], result);
        }
    }
}
