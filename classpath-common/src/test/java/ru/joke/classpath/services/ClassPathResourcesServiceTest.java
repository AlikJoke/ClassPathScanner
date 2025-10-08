package ru.joke.classpath.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClassPathResourcesServiceTest {

    @Test
    void testInstance() {
        final var service = ClassPathResourcesService.getInstance();
        assertNotNull(service, "Service instance must be not null");
        assertNotEquals(ClassPathResourcesService.getInstance(), service, "New service instance must be not equal");
    }
}
