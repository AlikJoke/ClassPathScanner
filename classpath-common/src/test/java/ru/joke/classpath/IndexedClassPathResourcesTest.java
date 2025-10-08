package ru.joke.classpath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class IndexedClassPathResourcesTest {

    private ClassPathResources resources;
    private ClassPathResource data1;
    private ClassPathResource data2;

    @BeforeEach
    void setUp() {
        this.data1 = mock(ClassPathResource.class);
        this.data2 = mock(ClassPathResource.class);

        this.resources = new IndexedClassPathResources();
        this.resources.add(this.data1);
        this.resources.add(this.data2);
    }

    @Test
    void testNoDuplications() {
        this.resources.add(this.data1);

        assertEquals(2, this.resources.size(), "Count of resources must be equal");
        assertTrue(this.resources.contains(this.data1), "Collection must contain resource");
        assertTrue(this.resources.contains(this.data2), "Collection must contain resource");
    }

    @Test
    void testSelection() {
        final var any = this.resources.any();
        assertNotNull(any, "Selected item must be not null");
        assertTrue(any.isPresent(), "Selected item must present");
        assertTrue(this.data1 == any.get() || this.data2 == any.get(), "Selected item must be equal to one of items in collection");
    }

    @Test
    void testEmptyCollection() {
        final var coll = new IndexedClassPathResources();
        assertEquals(0, coll.size(), "Collection must be empty");
        assertNotNull(coll.any(), "Selection result must be not null");
        assertTrue(coll.any().isEmpty(), "Item must not present");
    }
}
