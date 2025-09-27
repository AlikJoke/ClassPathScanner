package ru.joke.classpath.scanner.impl;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassResource;
import ru.joke.classpath.scanner.ClassPathScanner;
import ru.joke.classpath.scanner.ClassPathScannerBuilder;
import ru.joke.classpath.scanner.ClassPathScannerEngine;
import ru.joke.classpath.scanner.InvalidRequestSyntaxException;
import ru.joke.classpath.scanner.impl.engines.ExtendedClassPathScannerEngine;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Predicate;

public final class DefaultClassPathScannerBuilder implements ClassPathScannerBuilder {

    private final ExtendedClassPathScannerEngine engine;

    public DefaultClassPathScannerBuilder(final ClassPathScannerEngine engine) {
        if (!(engine instanceof ExtendedClassPathScannerEngine)) {
            throw new ClassCastException();
        }

        this.engine = (ExtendedClassPathScannerEngine) engine;
    }

    @Override
    public Begin begin() {
        return new CompoundFilter(null);
    }

    private class CompoundFilter implements Begin, LogicalOperations, End {

        private final CompoundFilter parent;

        private Predicate<ClassPathResource> filter = r -> true;
        private ClassPathScannerBuilder.Operator operator = ClassPathScannerBuilder.Operator.AND;
        private boolean negate;

        private CompoundFilter(final CompoundFilter parent) {
            this.parent = parent;
        }

        @Override
        public Begin begin() {
            return new CompoundFilter(this);
        }

        @Override
        public LogicalOperations inPackage(String packageName) {
            return appendCondition(r -> r.packageName().startsWith(packageName));
        }

        @Override
        public LogicalOperations inModule(String moduleName) {
            return appendCondition(r -> r.module().equals(moduleName));
        }

        @Override
        public LogicalOperations annotatedBy(Class<? extends Annotation> annotation) {
            return appendCondition(r -> contains(r.annotations(), annotation));
        }

        @Override
        public LogicalOperations withAlias(String alias) {
            return appendCondition(r -> r.aliases().contains(alias));
        }

        @Override
        public LogicalOperations implementsInterface(Class<?> interfaceClass) {
            return appendCondition(
                    r -> r instanceof ClassResource<?> cr && contains(cr.interfaces(), interfaceClass)
            );
        }

        @Override
        public LogicalOperations targetTypes(ClassPathResource.Type... types) {
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
        public LogicalOperations subClassOf(Class<?> superClass) {
            return appendCondition(
                    r -> r instanceof ClassResource<?> cr && contains(cr.superClasses(), superClass)
            );
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
                throw new InvalidRequestSyntaxException("Query creation must be called after the end of the main begin expression");
            }

            return new PredicateBasedClassPathScanner(this.negate ? this.filter.negate() : this.filter, engine);
        }

        private CompoundFilter appendCondition(final Predicate<ClassPathResource> condition) {
            this.filter = switch (this.operator) {
                case OR -> this.filter.or(this.negate ? condition.negate() : condition);
                case AND -> this.filter.and(this.negate ? condition.negate() : condition);
                case NOT -> condition.negate();
            };
            return this;
        }

        private boolean contains(final Set<ClassPathResource.ClassReference<?>> refs, final Class<?> type) {
            for (final var ref : refs) {
                if (ref.canonicalName().equals(type.getCanonicalName())) {
                    return true;
                }
            }

            return false;
        }
    }
}