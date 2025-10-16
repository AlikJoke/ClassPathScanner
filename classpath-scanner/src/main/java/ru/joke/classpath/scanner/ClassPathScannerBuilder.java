package ru.joke.classpath.scanner;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassResource;
import ru.joke.classpath.scanner.internal.DefaultClassPathScannerBuilder;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Scanner Builder.<br>
 * Provides a fluent API for building scan requests.<br>
 * For a usage example, refer to the {@link ClassPathScanner} documentation.
 * Implementation may not be thread-safe.
 *
 * @author Alik
 * @see ClassPathScanner
 */
public interface ClassPathScannerBuilder {

    /**
     * Begins the construction.<br>
     * Calling this method is equivalent to calling the {@link #begin(boolean)} method with the argument {@code false}.
     *
     * @return {@link Begin} object for further construction; cannot be {@code null}.
     * @see #begin(boolean)
     */
    default Begin begin() {
        return begin(false);
    }

    /**
     * Begins constructing a scanner with the ability to override the engine's default scan scope.
     *
     * @param overrideDefaultEngineScope flag indicating whether the engine's default scan scope should be overridden.
     * @return {@link Begin} object for further construction; cannot be {@code null}.
     */
    Begin begin(boolean overrideDefaultEngineScope);

    /**
     * Aggregation of different scan criteria.
     *
     * @see End
     * @see LogicalOperations
     */
    interface Begin {

        /**
         * Starts building a scan sub-query.<br>
         * This is analogous to enclosing a part of an expression in parentheses.<br>
         * After all expressions, this block must be terminated with a call to the {@link End#end()} method.
         *
         * @return new aggregation object for further construction; cannot be {@code null}.
         * @see End
         * @see LogicalOperations
         */
        Begin begin();

        /**
         * Excludes all resources whose package matches the provided package.<br>
         * If the {@code exactMatch} parameter is set to {@code true}, resources in sub-packages will also be excluded.
         *
         * @param exactMatch flag indicating whether all sub-packages of this package should also be included in the exclusion.
         * @param packageName provided package to exclude; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations excludeResourcesInPackage(boolean exactMatch, String packageName);

        /**
         * Excludes all resources whose packages match the provided package or are a sub-package of the provided package.<br>
         * Calling this method is equivalent to calling the {@link #excludeResourcesInPackage(boolean, String)} method with the argument {@code exactMatch=false}.
         *
         * @param packageName provided package to exclude; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        default LogicalOperations excludeResourcesInPackage(String packageName) {
            return excludeResourcesInPackage(false, packageName);
        }

        /**
         * Excludes all resources whose packages match any of the packages in the provided list.<br>
         * If the {@code exactMatch} parameter is set to {@code true}, resources in sub-packages of these packages will also be excluded.
         *
         * @param exactMatch flag indicating whether all sub-packages of these packages should also be included in the exclusion.
         * @param packageNames provided packages to exclude; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations excludeResourcesInPackages(boolean exactMatch, String... packageNames);

        /**
         * Excludes all resources whose packages match one of the packages in the provided list, or are a sub-package
         * of one of the packages in the provided list.<br>
         * Calling this method is equivalent to calling the {@link #excludeResourcesInPackages(boolean, String...)} method with the argument {@code exactMatch=false}.
         *
         * @param packageNames provided packages to exclude; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        default LogicalOperations excludeResourcesInPackages(String... packageNames) {
            return excludeResourcesInPackages(false, packageNames);
        }

        /**
         * Excludes all resources whose packages match to the provided pattern.<br>
         *
         * @param packagesPattern provided packages pattern to exclude; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations excludeResourcesInPackages(Pattern packagesPattern);

        /**
         * Includes all resources whose package matches the provided package.<br>
         * If the {@code exactMatch} parameter is set to {@code true}, resources in sub-packages will also be included.
         *
         * @param exactMatch flag indicating whether all sub-packages of this package should also be included.
         * @param packageName provided package to include; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations includeResourcesInPackage(boolean exactMatch, String packageName);

        /**
         * Includes all resources whose packages match the provided package or are a sub-package of the provided package.<br>
         * Calling this method is equivalent to calling the {@link #includeResourcesInPackage(boolean, String)} method with the argument {@code exactMatch=false}.
         *
         * @param packageName provided package to include; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        default LogicalOperations includeResourcesInPackage(String packageName) {
            return includeResourcesInPackage(false, packageName);
        }

        /**
         * Includes all resources whose packages match to the provided pattern.<br>
         *
         * @param packagesPattern provided packages pattern to include; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations includeResourcesInPackages(Pattern packagesPattern);

        /**
         * Includes all resources whose packages match any of the packages in the provided list.<br>
         * If the {@code exactMatch} parameter is set to {@code true}, resources in sub-packages of these packages will also be included.
         *
         * @param exactMatch flag indicating whether all sub-packages of these packages should also be included.
         * @param packageNames provided packages to include; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations includeResourcesInPackages(boolean exactMatch, String... packageNames);

        /**
         * Includes all resources whose packages match one of the packages in the provided list, or are a sub-package
         * of one of the packages in the provided list.<br>
         * Calling this method is equivalent to calling the {@link #includeResourcesInPackages(boolean, String...)} method with the argument {@code exactMatch=false}.
         *
         * @param packageNames provided packages to include; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        default LogicalOperations includeResourcesInPackages(String... packageNames) {
            return includeResourcesInPackages(false, packageNames);
        }

        /**
         * Excludes all resources whose JPMS module match the provided module.<br>
         *
         * @param moduleName provided module name to exclude; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations excludeResourcesInModule(String moduleName);

        /**
         * Excludes all resources whose JPMS module match the one of the provided list.<br>
         *
         * @param moduleNames provided modules to exclude; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations excludeResourcesInModules(String... moduleNames);

        /**
         * Excludes all resources whose JPMS module match to the provided pattern.<br>
         *
         * @param modulesPattern provided pattern of modules to exclude; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations excludeResourcesInModules(Pattern modulesPattern);

        /**
         * Includes in the scan request only those resources whose JPMS module match the provided module.<br>
         *
         * @param moduleName provided module to include; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations includeResourcesInModule(String moduleName);

        /**
         * Includes in the scan request only those resources whose JPMS module match the one of the provided list.<br>
         *
         * @param moduleNames provided modules to include; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations includeResourcesInModules(String... moduleNames);

        /**
         * Includes in the scan request only those resources whose JPMS module match to the provided pattern.<br>
         *
         * @param modulesPattern provided pattern of modules to include; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations includeResourcesInModules(Pattern modulesPattern);

        /**
         * Includes in the scan request only those resources that are annotated with the provided annotation.
         *
         * @param annotation provided annotation type; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations annotatedBy(Class<? extends Annotation> annotation);

        /**
         * Includes in the scan request only those resources that are annotated with at least one of the provided annotations.
         *
         * @param annotations provided annotation types; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        @SuppressWarnings("unchecked")
        LogicalOperations annotatedByAnyOf(Class<? extends Annotation>... annotations);

        /**
         * Includes in the scan request only those resources that are annotated with all the provided annotations.
         *
         * @param annotations provided annotation types; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        @SuppressWarnings("unchecked")
        LogicalOperations annotatedByAllOf(Class<? extends Annotation>... annotations);

        /**
         * Includes in the scan request only those resources that have the provided alias.
         *
         * @param alias provided alias; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations withAlias(String alias);

        /**
         * Includes in the scan request only those resources for which at least one alias matches the provided pattern.
         *
         * @param aliasesPattern provided pattern; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations withAnyOfAliases(Pattern aliasesPattern);

        /**
         * Includes in the scan request only those resources for which all existing aliases match the provided pattern.
         *
         * @param aliasesPattern provided pattern; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations withAllOfAliases(Pattern aliasesPattern);

        /**
         * Includes in the scan request only those resources that have at least one alias that matches any alias
         * in the provided list.
         *
         * @param aliases provided aliases; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations withAnyOfAliases(String... aliases);

        /**
         * Includes in the scan request only those resources for which all existing aliases are contained within the
         * provided list (i.e., all aliases in the list are present among the resource's aliases).
         *
         * @param aliases provided aliases; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations withAllOfAliases(String... aliases);

        /**
         * Includes in the scan request only those classes that implement or inherit the provided interface.
         *
         * @param interfaceClass provided interface type; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations implementsInterface(Class<?> interfaceClass);

        /**
         * Includes in the scan request only those class resources that implement at least one of the provided interfaces.
         *
         * @param interfaceClasses provided interface types; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations implementsAnyOfInterfaces(Class<?>... interfaceClasses);

        /**
         * Includes in the scan request only those class resources that implement all the provided interfaces.
         *
         * @param interfaceClasses provided interface types; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations implementsAllOfInterfaces(Class<?>... interfaceClasses);

        /**
         * Includes in the scan request only those class resources that have the provided class
         * in their inheritance hierarchy (i.e., among their superclasses).
         *
         * @param superClass provided superclass type; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations extendsBy(Class<?> superClass);

        /**
         * Includes in the scan request only those class resources that have at least one of the provided
         * classes in their inheritance hierarchy (i.e., among their superclasses).
         *
         * @param superClass provided superclass types; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations extendsByAnyOf(Class<?>... superClass);

        /**
         * Includes in the scan request only those resources that have the provided type.
         *
         * @param type provided resource type; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations includeResourceType(ClassPathResource.Type type);

        /**
         * Includes in the scan request only those resources that have any of the types from the provided list.
         *
         * @param types provided resource types; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations includeResourceTypes(ClassPathResource.Type... types);

        /**
         * Excludes from the scan request resources that have the provided type.
         *
         * @param type provided resource type; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations excludeResourceType(ClassPathResource.Type type);

        /**
         * Excludes from the scan request resources that have any of the types from the provided list.
         *
         * @param types provided resource types; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations excludeResourceTypes(ClassPathResource.Type... types);

        /**
         * Includes in the scan request only those resources that have the provided modifier among their modifiers.
         *
         * @param modifier provided modifier; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations hasModifier(ClassPathResource.Modifier modifier);

        /**
         * Includes in the scan request only those resources that have all the provided modifiers among their modifiers.
         *
         * @param modifiers provided modifiers; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations hasAllOfModifiers(ClassPathResource.Modifier... modifiers);

        /**
         * Includes in the scan request only those resources that have at least one of the provided modifiers among their modifiers.
         *
         * @param modifiers provided modifiers; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations hasAnyOfModifiers(ClassPathResource.Modifier... modifiers);

        /**
         * Excludes from the scan request class resources that have the provided kind.
         *
         * @param kind provided class kind; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations excludeClassKind(ClassResource.Kind kind);

        /**
         * Excludes from the scan request class resources that have any of the kinds from the provided list.
         *
         * @param kinds provided class kind; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations excludeClassKinds(ClassResource.Kind... kinds);

        /**
         * Includes in the scan request only those class resources that have the provided kind.
         *
         * @param kind provided class kind; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations includeClassKind(ClassResource.Kind kind);

        /**
         * Excludes from the scan request class resources that have any of the kinds from the provided list.
         *
         * @param kinds provided class kinds; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations includeClassKinds(ClassResource.Kind... kinds);

        /**
         * Allows defining a custom resource filter for filtering by specific criteria.
         *
         * @param filter custom resource filter; cannot be {@code null}.
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations filter(Predicate<ClassPathResource> filter);

        /**
         * Sets a filter that includes all indexed classpath resources.
         *
         * @return {@link LogicalOperations} object for further construction; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations all();

        /**
         * Sets the logical negation for the subsequent expression.
         *
         * @return current object for further construction; cannot be {@code null}.
         */
        Begin not();
    }

    /**
     * Logical operations for composing complex resource filtering criteria.
     *
     * @see End
     * @see Begin
     */
    interface LogicalOperations extends End {

        /**
         * Applies a logical AND to the filtering conditions.
         *
         * @return current builder object; cannot be {@code null}.
         * @see Begin
         */
        Begin and();

        /**
         * Applies a logical OR to the filtering conditions.
         *
         * @return current builder object; cannot be {@code null}.
         * @see Begin
         */
        Begin or();
    }

    /**
     * A set of terminal operations that allow either constructing a scanner with all its conditions, or completing a scan sub-query.
     *
     * @see LogicalOperations
     * @see ClassPathScanner
     */
    interface End {

        /**
         * Completes the current filtering expression (or sub-expression).
         *
         * @return previous (parent) filtering expression; cannot be {@code null}.
         * @see LogicalOperations
         */
        LogicalOperations end();

        /**
         * Builds the scanner object based on this builder.<br>
         * This is terminal operation.
         *
         * @return scanner object; cannot be {@code null}.
         * @see ClassPathScanner
         */
        ClassPathScanner build();
    }

    /**
     * Creates a builder instance for constructing a scanner object.<br>
     * The returned object is not thread-safe.
     *
     * @return builder instance; cannot be {@code null}.
     * @see Begin
     */
    static ClassPathScannerBuilder create() {
        return new DefaultClassPathScannerBuilder();
    }
}
