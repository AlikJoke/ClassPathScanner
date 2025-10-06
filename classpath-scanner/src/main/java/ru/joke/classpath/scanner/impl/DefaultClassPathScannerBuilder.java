package ru.joke.classpath.scanner.impl;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassResource;
import ru.joke.classpath.scanner.ClassPathScanner;
import ru.joke.classpath.scanner.ClassPathScannerBuilder;
import ru.joke.classpath.scanner.InvalidApiUsageException;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class DefaultClassPathScannerBuilder implements ClassPathScannerBuilder {

    @Override
    public Begin begin(boolean overrideDefaultEngineScope) {
        return new CompoundFilter(null, overrideDefaultEngineScope);
    }

    private static class CompoundFilter implements Begin, LogicalOperations, End {

        private final CompoundFilter parent;
        private final boolean overrideDefaultEngineScope;

        private Predicate<ClassPathResource> filter = r -> true;
        private ClassPathScannerBuilder.Operator operator = ClassPathScannerBuilder.Operator.AND;
        private boolean negate;

        private CompoundFilter(final CompoundFilter parent, final boolean overrideDefaultEngineScope) {
            this.parent = parent;
            this.overrideDefaultEngineScope = overrideDefaultEngineScope;
        }

        private CompoundFilter(final CompoundFilter parent) {
            this(parent, false);
        }

        @Override
        public Begin begin() {
            return new CompoundFilter(this);
        }

        @Override
        public LogicalOperations includeResourcesInPackage(boolean exactMatch, String packageName) {
            return appendCondition(
                    r -> exactMatch
                            ? r.packageName().equals(packageName)
                            : r.packageName().startsWith(packageName)
            );
        }

        @Override
        public LogicalOperations includeResourcesInPackages(Pattern packagesPattern) {
            return appendCondition(r -> packagesPattern.matcher(r.packageName()).matches());
        }

        @Override
        public LogicalOperations includeResourcesInPackages(boolean exactMatch, String... packageNames) {
            return appendCondition(r -> contains(r.packageName(), exactMatch, packageNames));
        }

        @Override
        public LogicalOperations excludeResourcesInPackage(boolean exactMatch, String packageName) {
            return appendCondition(
                    r -> exactMatch
                            ? !r.packageName().startsWith(packageName)
                            : !r.packageName().equals(packageName)
            );
        }

        @Override
        public LogicalOperations excludeResourcesInPackages(boolean exactMatch, String... packageNames) {
            return appendCondition(r -> !contains(r.packageName(), exactMatch, packageNames));
        }

        @Override
        public LogicalOperations excludeResourcesInPackages(Pattern packagesPattern) {
            return appendCondition(r -> !packagesPattern.matcher(r.packageName()).matches());
        }

        @Override
        public LogicalOperations includeResourcesInModule(String moduleName) {
            return appendCondition(r -> r.module().equals(moduleName));
        }

        @Override
        public LogicalOperations includeResourcesInModules(String... moduleNames) {
            return appendCondition(r -> contains(r.module(), true, moduleNames));
        }

        @Override
        public LogicalOperations includeResourcesInModules(Pattern modulesPattern) {
            return appendCondition(r -> modulesPattern.matcher(r.module()).matches());
        }

        @Override
        public LogicalOperations excludeResourcesInModule(String moduleName) {
            return appendCondition(r -> !r.module().equals(moduleName));
        }

        @Override
        public LogicalOperations excludeResourcesInModules(String... moduleNames) {
            return appendCondition(r -> !contains(r.module(), true, moduleNames));
        }

        @Override
        public LogicalOperations excludeResourcesInModules(Pattern modulesPattern) {
            return appendCondition(r -> !modulesPattern.matcher(r.module()).matches());
        }

        @Override
        public LogicalOperations annotatedBy(Class<? extends Annotation> annotation) {
            return appendCondition(r -> contains(r.annotations(), annotation));
        }

        @Override
        @SafeVarargs
        public final LogicalOperations annotatedByAnyOf(Class<? extends Annotation>... annotations) {
            return appendCondition(r -> containsAny(r.annotations(), annotations));
        }

        @Override
        @SafeVarargs
        public final LogicalOperations annotatedByAllOf(Class<? extends Annotation>... annotations) {
            return appendCondition(r -> containsAll(r.annotations(), annotations));
        }

        @Override
        public LogicalOperations withAlias(String alias) {
            return appendCondition(r -> r.aliases().contains(alias));
        }

        @Override
        public LogicalOperations withAnyOfAliases(Pattern aliasesPattern) {
            return appendCondition(r -> r.aliases().stream().anyMatch(a -> aliasesPattern.matcher(a).matches()));
        }

        @Override
        public LogicalOperations withAllOfAliases(Pattern aliasesPattern) {
            return appendCondition(r -> r.aliases().stream().allMatch(a -> aliasesPattern.matcher(a).matches()));
        }

        @Override
        public LogicalOperations withAnyOfAliases(String... aliases) {
            return appendCondition(r -> containsAny(r.aliases(), aliases));
        }

        @Override
        public LogicalOperations withAllOfAliases(String... aliases) {
            return appendCondition(r -> containsAll(r.aliases(), aliases));
        }

        @Override
        public LogicalOperations implementsInterface(Class<?> interfaceClass) {
            return appendCondition(
                    r -> r instanceof ClassResource<?> cr && contains(cr.interfaces(), interfaceClass)
            );
        }

        @Override
        public LogicalOperations implementsAnyOfInterfaces(Class<?>... interfaceClasses) {
            return appendCondition(
                    r -> r instanceof ClassResource<?> cr && containsAny(cr.interfaces(), interfaceClasses)
            );
        }

        @Override
        public LogicalOperations implementsAllOfInterfaces(Class<?>... interfaceClasses) {
            return appendCondition(
                    r -> r instanceof ClassResource<?> cr && containsAll(cr.interfaces(), interfaceClasses)
            );
        }

        @Override
        public LogicalOperations includeResourceType(ClassPathResource.Type type) {
            return appendCondition(r -> r.type() == type);
        }

        @Override
        public LogicalOperations includeResourceTypes(ClassPathResource.Type... types) {
            return appendCondition(
                    r -> {
                        if (types == null || types.length == 0) {
                            return true;
                        }

                        for (var type : types) {
                            if (type == r.type()) {
                                return true;
                            }
                        }

                        return false;
                    }
            );
        }

        @Override
        public LogicalOperations excludeResourceType(ClassPathResource.Type type) {
            return appendCondition(r -> r.type() != type);
        }

        @Override
        public LogicalOperations excludeResourceTypes(ClassPathResource.Type... types) {
            return appendCondition(
                    r -> {
                        if (types == null) {
                            return true;
                        }

                        for (var type : types) {
                            if (type == r.type()) {
                                return false;
                            }
                        }

                        return true;
                    }
            );
        }

        @Override
        public LogicalOperations extendsBy(Class<?> superClass) {
            return appendCondition(
                    r -> r instanceof ClassResource<?> cr && contains(cr.superClasses(), superClass)
            );
        }

        @Override
        public LogicalOperations extendsByAnyOf(Class<?>... superClasses) {
            return appendCondition(
                    r -> r instanceof ClassResource<?> cr && containsAny(cr.superClasses(), superClasses)
            );
        }

        @Override
        public LogicalOperations hasModifier(ClassPathResource.Modifier modifier) {
            return appendCondition(r -> r.modifiers().contains(modifier));
        }

        @Override
        public LogicalOperations hasAllOfModifiers(ClassPathResource.Modifier... modifiers) {
            return appendCondition(r -> containsAll(r.modifiers(), modifiers));
        }

        @Override
        public LogicalOperations hasAnyOfModifiers(ClassPathResource.Modifier... modifiers) {
            return appendCondition(r -> containsAny(r.modifiers(), modifiers));
        }

        @Override
        public LogicalOperations excludeClassKind(ClassResource.Kind kind) {
            return appendCondition(
                    r -> r instanceof ClassResource<?> cr && cr.kind() != kind
            );
        }

        @Override
        public LogicalOperations includeClassKind(ClassResource.Kind kind) {
            return appendCondition(
                    r -> r instanceof ClassResource<?> cr && cr.kind() == kind
            );
        }

        @Override
        public LogicalOperations excludeClassKinds(ClassResource.Kind... kinds) {
            return appendCondition(
                    r -> {
                        if (r instanceof ClassResource<?> cr) {
                            for (ClassResource.Kind kind : kinds) {
                                if (cr.kind() == kind) {
                                    return false;
                                }
                            }
                        }

                        return true;
                    }
            );
        }

        @Override
        public LogicalOperations includeClassKinds(ClassResource.Kind... kinds) {
            return appendCondition(
                    r -> {
                        if (r instanceof ClassResource<?> cr) {
                            for (ClassResource.Kind kind : kinds) {
                                if (cr.kind() == kind) {
                                    return true;
                                }
                            }
                        }

                        return false;
                    }
            );
        }

        @Override
        public LogicalOperations filter(Predicate<ClassPathResource> filter) {
            return appendCondition(filter);
        }

        @Override
        public LogicalOperations all() {
            return appendCondition(r -> true);
        }

        @Override
        public Begin not() {
            this.negate = true;
            return this;
        }

        @Override
        public Begin and() {
            this.negate = false;
            this.operator = ClassPathScannerBuilder.Operator.AND;
            return this;
        }

        @Override
        public Begin or() {
            this.negate = false;
            this.operator = ClassPathScannerBuilder.Operator.OR;
            return this;
        }

        @Override
        public LogicalOperations end() {
            this.parent.appendCondition(this.filter);
            this.negate = false;
            return this.parent;
        }

        @Override
        public ClassPathScanner build() {
            if (this.parent != null) {
                throw new InvalidApiUsageException("Query creation must be called after the end of the main begin expression");
            }

            final var resultFilter = this.negate ? this.filter.negate() : this.filter;
            return new PredicateBasedClassPathScanner() {

                @Override
                public boolean overrideDefaultEngineScope() {
                    return overrideDefaultEngineScope;
                }

                @Override
                public boolean test(ClassPathResource resource) {
                    return resultFilter.test(resource);
                }
            };
        }

        private CompoundFilter appendCondition(final Predicate<ClassPathResource> condition) {
            this.filter = switch (this.operator) {
                case OR -> this.filter.or(this.negate ? condition.negate() : condition);
                case AND -> this.filter.and(this.negate ? condition.negate() : condition);
                case NOT -> condition.negate();
            };
            return this;
        }

        private boolean contains(
                final Set<ClassPathResource.ClassReference<?>> refs,
                final Class<?> type
        ) {
            for (final var ref : refs) {
                if (ref.canonicalName().equals(type.getCanonicalName())) {
                    return true;
                }
            }

            return false;
        }

        private boolean containsAll(
                final Set<ClassPathResource.ClassReference<?>> refs,
                final Class<?>... types
        ) {
            for (final var type : types) {
                if (!contains(refs, type)) {
                    return false;
                }
            }

            return true;
        }

        private boolean containsAny(
                final Set<ClassPathResource.ClassReference<?>> refs,
                final Class<?>... types
        ) {
            if (types.length == 0) {
                return true;
            }

            for (final var type : types) {
                if (contains(refs, type)) {
                    return true;
                }
            }

            return false;
        }

        @SafeVarargs
        private <E> boolean containsAll(
                final Set<E> items,
                final E... targetItems
        ) {
            for (final var targetItem : targetItems) {
                if (!items.contains(targetItem)) {
                    return false;
                }
            }

            return true;
        }

        @SafeVarargs
        private <E> boolean containsAny(
                final Set<E> items,
                final E... targetItems
        ) {
            if (targetItems.length == 0) {
                return true;
            }

            for (final var targetItem : targetItems) {
                if (items.contains(targetItem)) {
                    return true;
                }
            }

            return false;
        }

        private boolean contains(
                final String targetItem,
                final boolean exactMatch,
                final String... items
        ) {
            for (final var item : items) {
                if (exactMatch && targetItem.equals(item) || !exactMatch && targetItem.startsWith(item)) {
                    return true;
                }
            }

            return false;
        }
    }
}