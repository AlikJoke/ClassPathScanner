package ru.joke.classpath.scanner;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassResource;
import ru.joke.classpath.scanner.impl.DefaultClassPathScannerBuilder;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public interface ClassPathScannerBuilder {

    Begin begin();

    interface Begin {

        Begin begin();

        LogicalOperations excludeResourcesInPackage(boolean exactMatch, String packageName);

        default LogicalOperations excludeResourcesInPackage(String packageName) {
            return excludeResourcesInPackage(false, packageName);
        }

        LogicalOperations excludeResourcesInPackages(boolean exactMatch, String... packageNames);

        default LogicalOperations excludeResourcesInPackages(String... packageNames) {
            return excludeResourcesInPackages(false, packageNames);
        }

        LogicalOperations includeResourcesInPackage(boolean exactMatch, String packageName);

        default LogicalOperations includeResourcesInPackage(String packageName) {
            return includeResourcesInPackage(false, packageName);
        }

        LogicalOperations includeResourcesInPackages(boolean exactMatch, String... packageNames);

        default LogicalOperations includeResourcesInPackages(String... packageNames) {
            return includeResourcesInPackages(false, packageNames);
        }

        LogicalOperations excludeResourcesInModule(String moduleName);

        LogicalOperations excludeResourcesInModules(String... moduleNames);

        LogicalOperations includeResourcesInModule(String moduleName);

        LogicalOperations includeResourcesInModules(String... moduleNames);

        LogicalOperations annotatedBy(Class<? extends Annotation> annotation);

        @SuppressWarnings("unchecked")
        LogicalOperations annotatedByAnyOf(Class<? extends Annotation>... annotations);

        @SuppressWarnings("unchecked")
        LogicalOperations annotatedByAllOf(Class<? extends Annotation>... annotations);

        LogicalOperations withAlias(String alias);

        LogicalOperations withAnyOfAliases(String... aliases);

        LogicalOperations withAllOfAliases(String... aliases);

        LogicalOperations implementsInterface(Class<?> interfaceClass);

        LogicalOperations implementsAnyOfInterfaces(Class<?>... interfaceClasses);

        LogicalOperations implementsAllOfInterfaces(Class<?>... interfaceClasses);

        LogicalOperations extendsBy(Class<?> superClass);

        LogicalOperations extendsByAnyOf(Class<?>... superClass);

        LogicalOperations includeResourceType(ClassPathResource.Type type);

        LogicalOperations includeResourceTypes(ClassPathResource.Type... types);

        LogicalOperations excludeResourceType(ClassPathResource.Type type);

        LogicalOperations excludeResourceTypes(ClassPathResource.Type... types);

        LogicalOperations hasModifier(ClassPathResource.Modifier modifier);

        LogicalOperations hasAllOfModifiers(ClassPathResource.Modifier... modifiers);

        LogicalOperations hasAnyOfModifiers(ClassPathResource.Modifier... modifiers);

        LogicalOperations excludeClassKind(ClassResource.Kind kind);

        LogicalOperations excludeClassKinds(ClassResource.Kind... kinds);

        LogicalOperations includeClassKind(ClassResource.Kind kind);

        LogicalOperations includeClassKinds(ClassResource.Kind... kinds);

        LogicalOperations filter(Predicate<ClassPathResource> filter);

        Begin not();
    }

    interface LogicalOperations extends End {

        Begin and();

        Begin or();
    }

    interface End {

        LogicalOperations end();

        ClassPathScanner build();
    }

    enum Operator {

        OR,

        AND,

        NOT
    }

    static ClassPathScannerBuilder create() {
        return new DefaultClassPathScannerBuilder(ClassPathScannerEngines.getDefaultEngine());
    }

    static ClassPathScannerBuilder create(ClassPathScannerEngine engine) {
        return new DefaultClassPathScannerBuilder(engine);
    }
}
