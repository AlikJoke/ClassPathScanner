package ru.joke.classpath.indexer.internal.factories;

import org.junit.jupiter.api.Test;
import ru.joke.classpath.ModuleResource;
import ru.joke.classpath.PackageResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.test_util.TestModuleElement;
import ru.joke.classpath.indexer.test_util.TestPackageElement;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DelegatingResourceFactoryTest {

    @Test
    void testDefaultFactoryCreation() {
        final var context = mock(ClassPathIndexingContext.class);
        final var factory = new DelegatingResourceFactory(context);

        final var delegateFactories = List.of(
                new ModuleResourceFactory(context),
                new PackageResourceFactory(context),
                new ClassFieldResourceFactory(context),
                new ClassResourceFactory(context),
                new ClassExecutableElementResourceFactory(context)
        );

        final var expectedSupportedTypes =
                delegateFactories
                        .stream()
                        .map(ClassPathResourceFactory::supportedTypes)
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet());
        final var actualSupportedTypes = factory.supportedTypes();
        assertEquals(expectedSupportedTypes, actualSupportedTypes, "Supported types must be equal");
    }

    @Test
    void testWhenNoDelegationFactory() {
        final var context = mock(ClassPathIndexingContext.class);
        final var moduleFactory = spy(new ModuleResourceFactory(context));

        final var factory = new DelegatingResourceFactory(
                context,
                List.of(moduleFactory)
        );

        final var packageElement = new TestPackageElement(getClass().getPackage(), null);
        final var result = factory.create(packageElement);

        assertNotNull(result, "Result of the factory must be not null always");
        assertTrue(result.isEmpty(), "Result of the factory must be empty");
        verify(moduleFactory, never()).create(any());
        verify(moduleFactory, never()).doCreate(any());
    }

    @Test
    void testDelegation() {
        final var context = mock(ClassPathIndexingContext.class);
        final var moduleFactory = spy(new ModuleResourceFactory(context));
        final var testModuleElement = new TestModuleElement(getClass().getModule());
        final var moduleResource = mock(ModuleResource.class);

        doReturn(moduleResource).when(moduleFactory).doCreate(testModuleElement);

        final var packageFactory = spy(new PackageResourceFactory(context));
        final var testPackage = new TestPackageElement(getClass().getPackage(), testModuleElement);
        final var packageResource = mock(PackageResource.class);

        doReturn(packageResource).when(packageFactory).doCreate(testPackage);

        final var factory = new DelegatingResourceFactory(
                context,
                List.of(moduleFactory, packageFactory)
        );

        assertEquals(moduleFactory.supportedTypes().size() + packageFactory.supportedTypes().size(), factory.supportedTypes().size(), "Supported types count must be equal");
        assertTrue(factory.supportedTypes().containsAll(moduleFactory.supportedTypes()), "Supported types must contain all types from delegate factory");
        assertTrue(factory.supportedTypes().containsAll(packageFactory.supportedTypes()), "Supported types must contain all types from delegate factory");

        final var moduleResult = factory.create(testModuleElement);

        assertNotNull(moduleResult, "Result of the factory must be not null always");
        assertFalse(moduleResult.isEmpty(), "Result of the factory must be not empty");
        assertEquals(moduleResource, moduleResult.get(), "Result of the factory must be equal");
        verify(moduleFactory, never()).create(any());
        verify(moduleFactory).doCreate(testModuleElement);

        final var packageResult = factory.create(testPackage);

        assertNotNull(packageResult, "Result of the factory must be not null always");
        assertFalse(packageResult.isEmpty(), "Result of the factory must be not empty");
        assertEquals(packageResource, packageResult.get(), "Result of the factory must be equal");
        verify(packageFactory, never()).create(any());
        verify(packageFactory).doCreate(testPackage);
    }
}
