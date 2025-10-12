package ru.joke.classpath.scanner.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassResource;
import ru.joke.classpath.scanner.ClassPathScannerBuilder;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.constant.Constable;
import java.util.*;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultClassPathScannerBuilderTest {

    private ClassPathScannerBuilder.Begin filter;

    @BeforeEach
    void setUp() {
        this.filter = new DefaultClassPathScannerBuilder().begin();
    }

    @Test
    void testIncludeResourcesInPackageWithExactMatch() {
        final var packageName = "ru.joke";
        final var ops = filter.includeResourcesInPackage(true, packageName);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.packageName()).thenReturn(packageName);
        checkFilterMatch(scanner, resource);

        when(resource.packageName()).thenReturn(packageName + ".sub");
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn("");
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testIncludeResourcesInPackageWithoutExactMatch() {
        final var packageName = "ru.joke";
        final var ops = filter.includeResourcesInPackage(false, packageName);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.packageName()).thenReturn(packageName);
        checkFilterMatch(scanner, resource);

        when(resource.packageName()).thenReturn(packageName + ".sub");
        checkFilterMatch(scanner, resource);

        when(resource.packageName()).thenReturn("ru");
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn("");
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testIncludeResourcesInPackagesByPattern() {
        final var packageNameRegexp = Pattern.compile("ru\\.joke\\..*");
        final var ops = filter.includeResourcesInPackages(packageNameRegexp);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.packageName()).thenReturn("ru.joke");
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn("ru.joke.sub");
        checkFilterMatch(scanner, resource);

        when(resource.packageName()).thenReturn("");
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testIncludeResourcesInPackagesWithExactMatch() {
        final var packageNames = new String[] { "ru.joke", "ru.test" };
        final var ops = filter.includeResourcesInPackages(true, packageNames);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.packageName()).thenReturn("ru.joke");
        checkFilterMatch(scanner, resource);

        when(resource.packageName()).thenReturn("ru.test");
        checkFilterMatch(scanner, resource);

        when(resource.packageName()).thenReturn("ru.joke.test");
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn("");
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testIncludeResourcesInPackagesWithoutExactMatch() {
        final var packageNames = new String[] { "ru.joke", "ru.test" };
        final var ops = filter.includeResourcesInPackages(packageNames);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.packageName()).thenReturn("ru.joke");
        checkFilterMatch(scanner, resource);

        when(resource.packageName()).thenReturn("com.test");
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn("ru.joke.test");
        checkFilterMatch(scanner, resource);

        when(resource.packageName()).thenReturn("");
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testExcludeResourcesInPackageWithExactMatch() {
        final var packageName = "ru.joke";
        final var ops = filter.excludeResourcesInPackage(true, packageName);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.packageName()).thenReturn(packageName);
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn("ru.joke.test");
        checkFilterMatch(scanner, resource);

        when(resource.packageName()).thenReturn(packageName);
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn("");
        checkFilterMatch(scanner, resource);
    }

    @Test
    void testExcludeResourcesInPackageWithoutExactMatch() {
        final var packageName = "ru.joke";
        final var ops = filter.excludeResourcesInPackage(packageName);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.packageName()).thenReturn(packageName);
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn("ru.joke.test");
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn(packageName);
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn("ru.test");
        checkFilterMatch(scanner, resource);

        when(resource.packageName()).thenReturn("");
        checkFilterMatch(scanner, resource);
    }

    @Test
    void testExcludeResourcesInPackagesWithExactMatch() {
        final var packageNames = new String[] { "ru.joke", "ru.test" };
        final var ops = filter.excludeResourcesInPackages(true, packageNames);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.packageName()).thenReturn("ru.joke");
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn("ru.test");
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn("ru.joke.sub");
        checkFilterMatch(scanner, resource);

        when(resource.packageName()).thenReturn("");
        checkFilterMatch(scanner, resource);
    }

    @Test
    void testExcludeResourcesInPackagesWithoutExactMatch() {
        final var packageNames = new String[] { "ru.joke", "ru.test" };
        final var ops = filter.excludeResourcesInPackages(packageNames);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.packageName()).thenReturn("ru.joke");
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn("ru.test");
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn("ru.joke.sub");
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn("ru.example");
        checkFilterMatch(scanner, resource);

        when(resource.packageName()).thenReturn("");
        checkFilterMatch(scanner, resource);
    }

    @Test
    void testExcludeResourcesInPackagesWithPattern() {
        final var pattern = Pattern.compile("ru\\.joke\\..*");
        final var ops = filter.excludeResourcesInPackages(pattern);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.packageName()).thenReturn("ru.joke.sub");
        checkFilterNotMatch(scanner, resource);

        when(resource.packageName()).thenReturn("ru.test");
        checkFilterMatch(scanner, resource);

        when(resource.packageName()).thenReturn("");
        checkFilterMatch(scanner, resource);
    }

    @Test
    void testIncludeResourcesInModule() {
        final var moduleName = "ru.joke.test_module";
        final var ops = filter.includeResourcesInModule(moduleName);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.module()).thenReturn(moduleName);
        checkFilterMatch(scanner, resource);

        when(resource.module()).thenReturn("ru.joke.test");
        checkFilterNotMatch(scanner, resource);

        when(resource.module()).thenReturn("");
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testIncludeResourcesInModules() {
        final var module1 = "ru.joke.test_module";
        final var module2 = "ru.joke.test";

        final var moduleNames = new String[] { module1, module2 };
        final var ops = filter.includeResourcesInModules(moduleNames);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.module()).thenReturn(module1);
        checkFilterMatch(scanner, resource);

        when(resource.module()).thenReturn(module2);
        checkFilterMatch(scanner, resource);

        when(resource.module()).thenReturn("ru.joke.test2");
        checkFilterNotMatch(scanner, resource);

        when(resource.module()).thenReturn("");
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testIncludeResourcesInModulesByPattern() {
        final var modulesPattern = Pattern.compile("ru.*");
        final var ops = filter.includeResourcesInModules(modulesPattern);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.module()).thenReturn("ru.joke.test");
        checkFilterMatch(scanner, resource);

        when(resource.module()).thenReturn("com.test");
        checkFilterNotMatch(scanner, resource);

        when(resource.module()).thenReturn("");
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testExcludeResourcesInModule() {
        final var moduleName = "ru.joke.test";
        final var ops = filter.excludeResourcesInModule(moduleName);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.module()).thenReturn("ru.joke.test");
        checkFilterNotMatch(scanner, resource);

        when(resource.module()).thenReturn("ru.joke.example");
        checkFilterMatch(scanner, resource);

        when(resource.module()).thenReturn("");
        checkFilterMatch(scanner, resource);
    }

    @Test
    void testExcludeResourcesInModules() {
        final var moduleNames = new String[] { "ru.joke.test", "ru.joke.example" };
        final var ops = filter.excludeResourcesInModules(moduleNames);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.module()).thenReturn("ru.joke.test");
        checkFilterNotMatch(scanner, resource);

        when(resource.module()).thenReturn("com.example");
        checkFilterMatch(scanner, resource);

        when(resource.module()).thenReturn("ru.joke.example");
        checkFilterNotMatch(scanner, resource);

        when(resource.module()).thenReturn("");
        checkFilterMatch(scanner, resource);
    }

    @Test
    void testExcludeResourcesInModulesByPattern() {
        final var modulesPattern = Pattern.compile("ru.*");
        final var ops = filter.excludeResourcesInModules(modulesPattern);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();

        final var resource = mock(ClassPathResource.class);
        when(resource.module()).thenReturn("ru.joke.test");
        checkFilterNotMatch(scanner, resource);

        when(resource.module()).thenReturn("com.test");
        checkFilterMatch(scanner, resource);

        when(resource.module()).thenReturn("");
        checkFilterMatch(scanner, resource);
    }

    @Test
    void testAnnotatedBy() {
        final var annotation = Deprecated.class;
        final var ops = filter.annotatedBy(annotation);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.annotations()).thenReturn(Set.of(createClassRef(annotation), createClassRef(ClassPathIndexed.class)));
        checkFilterMatch(scanner, resource);

        when(resource.annotations()).thenReturn(Set.of(createClassRef(ClassPathIndexed.class)));
        checkFilterNotMatch(scanner, resource);

        when(resource.annotations()).thenReturn(Set.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testAnnotatedByAnyOf() {
        @SuppressWarnings("unchecked")
        final Class<? extends Annotation>[] annotations = new Class[] { Deprecated.class, ClassPathIndexed.class };
        final var ops = filter.annotatedByAnyOf(annotations);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.annotations()).thenReturn(Set.of(createClassRef(Deprecated.class), createClassRef(ClassPathIndexed.class)));
        checkFilterMatch(scanner, resource);

        when(resource.annotations()).thenReturn(Set.of(createClassRef(ClassPathIndexed.class)));
        checkFilterMatch(scanner, resource);

        when(resource.annotations()).thenReturn(Set.of(createClassRef(Documented.class)));
        checkFilterNotMatch(scanner, resource);

        when(resource.annotations()).thenReturn(Set.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testAnnotatedByAllOf() {
        @SuppressWarnings("unchecked")
        final Class<? extends Annotation>[] annotations = new Class[] { Deprecated.class, ClassPathIndexed.class };
        final var ops = filter.annotatedByAllOf(annotations);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.annotations()).thenReturn(Set.of(createClassRef(Deprecated.class), createClassRef(ClassPathIndexed.class)));
        checkFilterMatch(scanner, resource);

        when(resource.annotations()).thenReturn(Set.of(createClassRef(Deprecated.class), createClassRef(ClassPathIndexed.class), createClassRef(Documented.class)));
        checkFilterMatch(scanner, resource);

        when(resource.annotations()).thenReturn(Set.of(createClassRef(ClassPathIndexed.class)));
        checkFilterNotMatch(scanner, resource);

        when(resource.annotations()).thenReturn(Set.of(createClassRef(Documented.class)));
        checkFilterNotMatch(scanner, resource);

        when(resource.annotations()).thenReturn(Set.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testWithAlias() {
        final var alias = "test";
        final var ops = filter.withAlias(alias);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.aliases()).thenReturn(Set.of("test", "test2"));
        checkFilterMatch(scanner, resource);

        when(resource.aliases()).thenReturn(Set.of("test2"));
        checkFilterNotMatch(scanner, resource);

        when(resource.aliases()).thenReturn(Set.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testWithAnyOfAliasesByPattern() {
        final var alias = Pattern.compile("test.+");
        final var ops = filter.withAnyOfAliases(alias);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.aliases()).thenReturn(Set.of("test", "test2"));
        checkFilterMatch(scanner, resource);

        when(resource.aliases()).thenReturn(Set.of("test"));
        checkFilterNotMatch(scanner, resource);

        when(resource.aliases()).thenReturn(Set.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testWithAllOfAliasesByPattern() {
        final var alias = Pattern.compile("test.+");
        final var ops = filter.withAllOfAliases(alias);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.aliases()).thenReturn(Set.of("test", "test2"));
        checkFilterNotMatch(scanner, resource);

        when(resource.aliases()).thenReturn(Set.of("test1", "test2"));
        checkFilterMatch(scanner, resource);

        when(resource.aliases()).thenReturn(Set.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testWithAnyOfAliases() {
        final var ops = filter.withAnyOfAliases("test1", "test2");
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.aliases()).thenReturn(Set.of("test", "test2"));
        checkFilterMatch(scanner, resource);

        when(resource.aliases()).thenReturn(Set.of("test"));
        checkFilterNotMatch(scanner, resource);

        when(resource.aliases()).thenReturn(Set.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testWithAllOfAliases() {
        final var ops = filter.withAllOfAliases("test1", "test2");
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.aliases()).thenReturn(Set.of("test1", "test2", "test3"));
        checkFilterMatch(scanner, resource);

        when(resource.aliases()).thenReturn(Set.of("test1"));
        checkFilterNotMatch(scanner, resource);

        when(resource.aliases()).thenReturn(Set.of("test"));
        checkFilterNotMatch(scanner, resource);

        when(resource.aliases()).thenReturn(Set.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testImplementsInterface() {
        final var ops = filter.implementsInterface(Serializable.class);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassResource.class);
        when(resource.interfaces()).thenReturn(Set.of(createClassRef(Serializable.class), createClassRef(Comparable.class)));
        checkFilterMatch(scanner, resource);

        when(resource.interfaces()).thenReturn(Set.of(createClassRef(Comparable.class)));
        checkFilterNotMatch(scanner, resource);

        when(resource.interfaces()).thenReturn(Set.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testImplementsAnyOfInterfaces() {
        final var ops = filter.implementsAnyOfInterfaces(Serializable.class, Comparable.class);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassResource.class);
        when(resource.interfaces()).thenReturn(Set.of(createClassRef(Serializable.class), createClassRef(Constable.class)));
        checkFilterMatch(scanner, resource);

        when(resource.interfaces()).thenReturn(Set.of(createClassRef(Constable.class)));
        checkFilterNotMatch(scanner, resource);

        when(resource.interfaces()).thenReturn(Set.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testImplementsAllOfInterfaces() {
        final var ops = filter.implementsAllOfInterfaces(Serializable.class, Comparable.class);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassResource.class);
        when(resource.interfaces()).thenReturn(Set.of(createClassRef(Serializable.class), createClassRef(Comparable.class), createClassRef(Constable.class)));
        checkFilterMatch(scanner, resource);

        when(resource.interfaces()).thenReturn(Set.of(createClassRef(Serializable.class)));
        checkFilterNotMatch(scanner, resource);

        when(resource.interfaces()).thenReturn(Set.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testExtendsBy() {
        final var ops = filter.extendsBy(AbstractCollection.class);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassResource.class);
        when(resource.superClasses()).thenReturn(List.of(createClassRef(AbstractCollection.class), createClassRef(AbstractList.class)));
        checkFilterMatch(scanner, resource);

        when(resource.superClasses()).thenReturn(List.of(createClassRef(AbstractList.class)));
        checkFilterNotMatch(scanner, resource);

        when(resource.superClasses()).thenReturn(List.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testExtendsByAnyOf() {
        final var ops = filter.extendsByAnyOf(AbstractCollection.class, AbstractList.class);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassResource.class);
        when(resource.superClasses()).thenReturn(List.of(createClassRef(AbstractCollection.class), createClassRef(AbstractList.class)));
        checkFilterMatch(scanner, resource);

        when(resource.superClasses()).thenReturn(List.of(createClassRef(AbstractList.class)));
        checkFilterMatch(scanner, resource);

        when(resource.superClasses()).thenReturn(List.of(createClassRef(AbstractSet.class)));
        checkFilterNotMatch(scanner, resource);

        when(resource.superClasses()).thenReturn(List.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testIncludeResourceType() {
        final var ops = filter.includeResourceType(ClassPathResource.Type.CONSTRUCTOR);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.type()).thenReturn(ClassPathResource.Type.CONSTRUCTOR);
        checkFilterMatch(scanner, resource);

        when(resource.type()).thenReturn(ClassPathResource.Type.CLASS);
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testIncludeResourceTypes() {
        final var ops = filter.includeResourceTypes(ClassPathResource.Type.CONSTRUCTOR, ClassPathResource.Type.METHOD);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.type()).thenReturn(ClassPathResource.Type.CONSTRUCTOR);
        checkFilterMatch(scanner, resource);

        when(resource.type()).thenReturn(ClassPathResource.Type.CLASS);
        checkFilterNotMatch(scanner, resource);

        when(resource.type()).thenReturn(ClassPathResource.Type.METHOD);
        checkFilterMatch(scanner, resource);
    }

    @Test
    void testExcludeResourceType() {
        final var ops = filter.excludeResourceType(ClassPathResource.Type.CONSTRUCTOR);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.type()).thenReturn(ClassPathResource.Type.CONSTRUCTOR);
        checkFilterNotMatch(scanner, resource);

        when(resource.type()).thenReturn(ClassPathResource.Type.CLASS);
        checkFilterMatch(scanner, resource);
    }

    @Test
    void testExcludeResourceTypes() {
        final var ops = filter.excludeResourceTypes(ClassPathResource.Type.CONSTRUCTOR, ClassPathResource.Type.METHOD);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.type()).thenReturn(ClassPathResource.Type.CONSTRUCTOR);
        checkFilterNotMatch(scanner, resource);

        when(resource.type()).thenReturn(ClassPathResource.Type.CLASS);
        checkFilterMatch(scanner, resource);

        when(resource.type()).thenReturn(ClassPathResource.Type.METHOD);
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testHasModifier() {
        final var ops = filter.hasModifier(ClassPathResource.Modifier.DEFAULT);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.modifiers()).thenReturn(Set.of(ClassPathResource.Modifier.DEFAULT, ClassPathResource.Modifier.FINAL));
        checkFilterMatch(scanner, resource);

        when(resource.modifiers()).thenReturn(Set.of(ClassPathResource.Modifier.ABSTRACT));
        checkFilterNotMatch(scanner, resource);

        when(resource.modifiers()).thenReturn(Set.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testHasAllOfModifiers() {
        final var ops = filter.hasAllOfModifiers(ClassPathResource.Modifier.DEFAULT, ClassPathResource.Modifier.FINAL);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.modifiers()).thenReturn(Set.of(ClassPathResource.Modifier.DEFAULT, ClassPathResource.Modifier.FINAL));
        checkFilterMatch(scanner, resource);

        when(resource.modifiers()).thenReturn(Set.of(ClassPathResource.Modifier.DEFAULT));
        checkFilterNotMatch(scanner, resource);

        when(resource.modifiers()).thenReturn(Set.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testHasAnyOfModifiers() {
        final var ops = filter.hasAnyOfModifiers(ClassPathResource.Modifier.DEFAULT, ClassPathResource.Modifier.FINAL);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.modifiers()).thenReturn(Set.of(ClassPathResource.Modifier.DEFAULT, ClassPathResource.Modifier.FINAL));
        checkFilterMatch(scanner, resource);

        when(resource.modifiers()).thenReturn(Set.of(ClassPathResource.Modifier.DEFAULT, ClassPathResource.Modifier.ABSTRACT));
        checkFilterMatch(scanner, resource);

        when(resource.modifiers()).thenReturn(Set.of());
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testExcludeClassKind() {
        final var ops = filter.excludeClassKind(ClassResource.Kind.INTERFACE);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassResource.class);
        when(resource.kind()).thenReturn(ClassResource.Kind.CLASS);
        checkFilterMatch(scanner, resource);

        when(resource.kind()).thenReturn(ClassResource.Kind.INTERFACE);
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testExcludeClassKinds() {
        final var ops = filter.excludeClassKinds(ClassResource.Kind.INTERFACE, ClassResource.Kind.CLASS);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassResource.class);
        when(resource.kind()).thenReturn(ClassResource.Kind.CLASS);
        checkFilterNotMatch(scanner, resource);

        when(resource.kind()).thenReturn(ClassResource.Kind.INTERFACE);
        checkFilterNotMatch(scanner, resource);

        when(resource.kind()).thenReturn(ClassResource.Kind.ANNOTATION);
        checkFilterMatch(scanner, resource);
    }

    @Test
    void testIncludeClassKind() {
        final var ops = filter.includeClassKind(ClassResource.Kind.INTERFACE);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassResource.class);
        when(resource.kind()).thenReturn(ClassResource.Kind.CLASS);
        checkFilterNotMatch(scanner, resource);

        when(resource.kind()).thenReturn(ClassResource.Kind.INTERFACE);
        checkFilterMatch(scanner, resource);
    }

    @Test
    void testIncludeClassKinds() {
        final var ops = filter.includeClassKinds(ClassResource.Kind.INTERFACE, ClassResource.Kind.CLASS);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassResource.class);
        when(resource.kind()).thenReturn(ClassResource.Kind.CLASS);
        checkFilterMatch(scanner, resource);

        when(resource.kind()).thenReturn(ClassResource.Kind.INTERFACE);
        checkFilterMatch(scanner, resource);

        when(resource.kind()).thenReturn(ClassResource.Kind.ANNOTATION);
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testAll() {
        final var ops = filter.all();
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        checkFilterMatch(scanner, resource);
    }

    @Test
    void testNotOperation() {
        final var ops = filter.not().includeClassKind(ClassResource.Kind.INTERFACE);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassResource.class);
        when(resource.kind()).thenReturn(ClassResource.Kind.CLASS);
        checkFilterMatch(scanner, resource);

        when(resource.kind()).thenReturn(ClassResource.Kind.INTERFACE);
        checkFilterNotMatch(scanner, resource);
    }

    @Test
    void testOrOperation() {
        final var ops = filter.includeClassKind(ClassResource.Kind.INTERFACE)
                                .or()
                              .withAlias("test");
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassResource.class);
        when(resource.kind()).thenReturn(ClassResource.Kind.INTERFACE);
        when(resource.aliases()).thenReturn(Set.of("test1"));
        checkFilterMatch(scanner, resource);

        when(resource.kind()).thenReturn(ClassResource.Kind.CLASS);
        when(resource.aliases()).thenReturn(Set.of("test"));
        checkFilterMatch(scanner, resource);

        when(resource.kind()).thenReturn(ClassResource.Kind.CLASS);
        when(resource.aliases()).thenReturn(Set.of("test1"));
        checkFilterNotMatch(scanner, resource);

        when(resource.kind()).thenReturn(ClassResource.Kind.INTERFACE);
        when(resource.aliases()).thenReturn(Set.of("test"));
        checkFilterMatch(scanner, resource);
    }

    @Test
    void testAndOperation() {
        final var ops = filter.includeClassKind(ClassResource.Kind.INTERFACE)
                                .and()
                              .withAlias("test");
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassResource.class);
        when(resource.kind()).thenReturn(ClassResource.Kind.INTERFACE);
        when(resource.aliases()).thenReturn(Set.of("test1"));
        checkFilterNotMatch(scanner, resource);

        when(resource.kind()).thenReturn(ClassResource.Kind.CLASS);
        when(resource.aliases()).thenReturn(Set.of("test"));
        checkFilterNotMatch(scanner, resource);

        when(resource.kind()).thenReturn(ClassResource.Kind.CLASS);
        checkFilterNotMatch(scanner, resource);

        when(resource.kind()).thenReturn(ClassResource.Kind.INTERFACE);
        when(resource.aliases()).thenReturn(Set.of("test"));
        checkFilterMatch(scanner, resource);
    }

    @Test
    void testCustomFilter() {
        final var ops = filter.filter(r -> r.type() == ClassPathResource.Type.CLASS);
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassPathResource.class);
        when(resource.type()).thenReturn(ClassPathResource.Type.CLASS);
        checkFilterMatch(scanner, resource);

        for (var type : EnumSet.complementOf(EnumSet.of(ClassPathResource.Type.CLASS))) {
            when(resource.type()).thenReturn(type);
            checkFilterNotMatch(scanner, resource);
        }
    }

    @Test
    void testCompoundOperation() {
        final var ops = filter.begin()
                                .includeClassKind(ClassResource.Kind.INTERFACE)
                                    .and()
                                .withAlias("test")
                              .end()
                              .and()
                                .not()
                                    .hasModifier(ClassPathResource.Modifier.PUBLIC)
                              .end();
        makeAppendedConditionResultCheck(ops);

        final var scanner = (PredicateBasedClassPathScanner) ops.build();
        final var resource = mock(ClassResource.class);
        when(resource.kind()).thenReturn(ClassResource.Kind.INTERFACE);
        when(resource.aliases()).thenReturn(Set.of("test1"));
        checkFilterNotMatch(scanner, resource);

        when(resource.kind()).thenReturn(ClassResource.Kind.INTERFACE);
        when(resource.aliases()).thenReturn(Set.of("test"));
        when(resource.modifiers()).thenReturn(Set.of());
        checkFilterMatch(scanner, resource);

        when(resource.kind()).thenReturn(ClassResource.Kind.INTERFACE);
        when(resource.aliases()).thenReturn(Set.of("test"));
        when(resource.modifiers()).thenReturn(Set.of(ClassPathResource.Modifier.PUBLIC));
        checkFilterNotMatch(scanner, resource);
    }

    private void makeAppendedConditionResultCheck(ClassPathScannerBuilder.LogicalOperations ops) {
        assertNotNull(ops, "Builder should return next object to chain operations");
    }

    private void checkFilterMatch(final PredicateBasedClassPathScanner scanner, final ClassPathResource resource) {
        assertTrue(scanner.test(resource), "Filter should be match");
    }

    private void checkFilterNotMatch(final PredicateBasedClassPathScanner scanner, final ClassPathResource resource) {
        assertFalse(scanner.test(resource), "Filter should be not match");
    }

    private <T> ClassPathResource.ClassReference<T> createClassRef(final Class<T> clazz) {
        return new ClassPathResource.ClassReference<>() {
            @Override
            public String canonicalName() {
                return clazz.getCanonicalName();
            }

            @Override
            public String binaryName() {
                return clazz.getName();
            }

            @Override
            public Class<T> toClass(ClassLoader loader) {
                return clazz;
            }
        };
    }
}
