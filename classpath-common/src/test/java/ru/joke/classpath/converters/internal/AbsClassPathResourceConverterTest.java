package ru.joke.classpath.converters.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.converters.Dictionary;
import ru.joke.classpath.test_util.TestDictionary;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

abstract class AbsClassPathResourceConverterTest<R extends ClassPathResource, C extends ConcreteClassPathResourceConverter<R>> {

    protected C converter;

    @BeforeEach
    void setUp() {
        this.converter = createConverter();
    }

    @Test
    void testSupportedType() {
        assertEquals(testResourceType(), this.converter.supportedType(), "Supported type of the converter must be equal");
    }

    @Test
    void testToString() {
        final var resource = createTestResource();
        final var dictionary = createDictionary(resource);

        final var result = this.converter.toString(resource, dictionary.reversedDictionary());
        assertEquals(getExpectedStringRepresentation(), result, "String representation of the resource must be equal");
    }

    @Test
    void testFromStringInvalid() {
        final var result = this.converter.fromString(testResourceType().alias() + "|||||", new TestDictionary(new HashMap<>()));
        assertNotNull(result, "Result of the converter must be always not null");
        assertTrue(result.isEmpty(), "Result of the converter must be empty");
    }
    
    @Test
    void testFromStringOk() throws Exception {
        final var sourceResource = createTestResource();
        final var result = this.converter.fromString(getExpectedStringRepresentation(), createDictionary(sourceResource));
        assertNotNull(result, "Result of converter must be always not null");
        assertTrue(result.isPresent(), "Result of of converter must present");

        final var resource = result.get();
        assertEquals(testResourceType(), resource.type(), "Type of resource must be equal");
        assertEquals(sourceResource.name(), resource.name(), "Resource name must be equal");
        assertEquals(sourceResource.id(), resource.id(), "Resource id must be equal");
        assertEquals(sourceResource.module(), resource.module(), "Module of resource must be equal");
        assertEquals(sourceResource.toStringDescription(), resource.toStringDescription(), "String description of resource must be equal");
        assertEquals(sourceResource.modifiers(), resource.modifiers(), "Modifiers must be equal");
        assertEquals(sourceResource.aliases(), resource.aliases(), "Aliases must be equal");
        assertEquals(sourceResource.annotations().size(), resource.annotations().size(), "Annotations count must be equal");
        assertEquals(resource.toStringDescription(), resource.toString(), "String description must be equal to result of toString method");
        assertEquals(sourceResource.packageName(), resource.packageName(), "Package name of the resource must be equal");

        final var resourceAnnotationsMap =
                resource.annotations()
                        .stream()
                        .collect(Collectors.toMap(ClassPathResource.ClassReference::binaryName, Function.identity()));
        for (final var annotation : sourceResource.annotations()) {
            final var resourceAnnotation = resourceAnnotationsMap.get(annotation.binaryName());

            assertNotNull(resourceAnnotation, "Annotation must be not null");
            assertEquals(annotation.toClass(), resourceAnnotation.toClass(), "Annotation type must be equal");
        }

        makeTypeSpecificChecks(sourceResource, resource);
    }

    protected Dictionary createDictionary(R resource) {

        final Map<String, String> dictionaryMap = new HashMap<>();
        if (!resource.module().isEmpty()) {
            dictionaryMap.put("0", resource.module());
        }

        if (!resource.packageName().isEmpty() && !dictionaryMap.containsValue(resource.packageName())) {
            dictionaryMap.put(String.valueOf(dictionaryMap.size()), resource.packageName());
        }

        if (!dictionaryMap.containsValue(resource.name())) {
            dictionaryMap.put(String.valueOf(dictionaryMap.size()), resource.name());
        }

        for (var alias : resource.aliases()) {
            if (!dictionaryMap.containsValue(alias)) {
                dictionaryMap.put(String.valueOf(dictionaryMap.size()), alias);
            }
        }

        for (var annotation : resource.annotations()) {
            if (!dictionaryMap.containsValue(annotation.binaryName())) {
                dictionaryMap.put(String.valueOf(dictionaryMap.size()), annotation.binaryName());
            }
        }

        fillDictionary(dictionaryMap, resource);

        return new TestDictionary(dictionaryMap);
    }

    void fillDictionary(Map<String, String> dictionaryMap, R resource) {
    }

    abstract void makeTypeSpecificChecks(R expected, R actual) throws Exception;

    abstract C createConverter();
    
    abstract ClassPathResource.Type testResourceType();
    
    abstract R createTestResource();
    
    abstract String getExpectedStringRepresentation();
}
