package ru.joke.classpath.indexer.internal;

import org.junit.jupiter.api.Test;
import ru.joke.classpath.indexer.test_util.TestModuleElement;
import ru.joke.classpath.indexer.test_util.TestPackageElement;
import ru.joke.classpath.indexer.test_util.TestTypeElement;
import ru.joke.classpath.indexer.test_util.fixtures.TestClass;

import javax.lang.model.element.Element;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ScanningResourcesFilterTest {

    @Test
    void testWhenNoFilterSet() {
        final var filter = new ScanningResourcesFilter(null, null);
        assertTrue(filter.test(mock(Element.class)), "Any element should be passed");
    }

    @Test
    void testWhenConcreteIncludedFilterSet() {
        final var filter = new ScanningResourcesFilter(TestClass.class.getPackageName(), null);
        final var module = new TestModuleElement(getClass().getModule());
        final var clazz = new TestTypeElement(TestClass.class);
        final var pkg = new TestPackageElement(TestClass.class.getPackage(), module);

        assertFalse(filter.test(module), "Module element should not be passed");
        assertFalse(filter.test(clazz), "Class element should not be passed");
        assertTrue(filter.test(pkg), "Package element should be passed");
    }

    @Test
    void testWhenMaskIncludedFilterSet() {
        testWhenMaskIncludedFilterSet(true);
        testWhenMaskIncludedFilterSet(false);
    }

    @Test
    void testWhenConcreteExcludedFilterSet() {
        final var filter = new ScanningResourcesFilter(null, TestClass.class.getPackageName());
        final var module = new TestModuleElement(getClass().getModule());
        final var clazz = new TestTypeElement(TestClass.class);
        final var pkg = new TestPackageElement(TestClass.class.getPackage(), module);

        assertTrue(filter.test(module), "Module element should be passed");
        assertTrue(filter.test(clazz), "Class element should be passed");
        assertFalse(filter.test(pkg), "Package element should not be passed");
    }

    @Test
    void testWhenMaskExcludedFilterSet() {
        testWhenMaskExcludedFilterSet(true);
        testWhenMaskExcludedFilterSet(false);
    }

    @Test
    void testWhenBothFiltersSet() {
        final var filter = new ScanningResourcesFilter("ru.joke.\\S+", TestClass.class.getCanonicalName() + ".\\S*");
        final var nestedClass = new TestTypeElement(TestClass.NestedClass.class);
        final var clazz = new TestTypeElement(TestClass.class);
        final var pkg = new TestPackageElement(TestClass.class.getPackage(), null);

        assertTrue(filter.test(clazz), "Class element should be passed");
        assertTrue(filter.test(pkg), "Package element should be passed");
        assertFalse(filter.test(nestedClass), "Nested class element should not be passed");
    }

    private void testWhenMaskIncludedFilterSet(boolean includeSameElement) {
        final var filter = new ScanningResourcesFilter(TestClass.class.getPackageName() + "\\S" + (includeSameElement ? "*" : "+"), null);
        final var nestedClass = new TestTypeElement(TestClass.NestedClass.class);
        final var clazz = new TestTypeElement(TestClass.class);
        final var pkg = new TestPackageElement(TestClass.class.getPackage(), null);

        if (includeSameElement) {
            assertTrue(filter.test(pkg), "Package element should be passed");
        } else {
            assertFalse(filter.test(pkg), "Package element should not be passed");
        }
        assertTrue(filter.test(clazz), "Class element should be passed");
        assertTrue(filter.test(nestedClass), "Nested class element should be passed");
    }

    private void testWhenMaskExcludedFilterSet(boolean excludeSameElement) {
        final var filter = new ScanningResourcesFilter(null, TestClass.class.getPackageName() + "\\S" + (excludeSameElement ? "*" : "+"));
        final var nestedClass = new TestTypeElement(TestClass.NestedClass.class);
        final var clazz = new TestTypeElement(TestClass.class);
        final var pkg = new TestPackageElement(TestClass.class.getPackage(), null);

        if (excludeSameElement) {
            assertFalse(filter.test(pkg), "Package element should not be passed");
        } else {
            assertTrue(filter.test(pkg), "Package element should be passed");
        }
        assertFalse(filter.test(clazz), "Class element should not be passed");
        assertFalse(filter.test(nestedClass), "Nested class element should not be passed");
    }
}
