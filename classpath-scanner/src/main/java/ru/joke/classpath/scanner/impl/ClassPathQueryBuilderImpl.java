package ru.joke.classpath.scanner.impl;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.scanner.ClassPathQuery;
import ru.joke.classpath.scanner.ClassPathQueryBuilder;
import ru.joke.classpath.scanner.InvalidQuerySyntaxException;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ClassPathQueryBuilderImpl implements ClassPathQueryBuilder {

    @Override
    public Begin begin() {
        return new CompoundFilter(null);
    }

    private static class CompoundFilter implements Begin, LogicalOperations, End {

        private final CompoundFilter parent;

        private Predicate<ClassPathResource> filter = r -> true;
        private ClassPathQueryBuilder.Operator operator = ClassPathQueryBuilder.Operator.AND;
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
            return appendCondition(r -> r.annotations().stream().map(ClassPathResource.ClassReference::canonicalName).collect(Collectors.toSet()).contains(annotation.getCanonicalName()));
        }

        @Override
        public LogicalOperations withAlias(String alias) {
            return appendCondition(r -> r.aliases().contains(alias));
        }

        @Override
        public LogicalOperations implementsInterface(Class<?> interfaceClass) {
            return appendCondition(
                    r -> r instanceof ClassPathResource.ClassResource<?> cr && contains(cr.interfaces(), interfaceClass)
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
                    r -> r instanceof ClassPathResource.ClassResource<?> cr && contains(cr.superClasses(), superClass)
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
            this.operator = ClassPathQueryBuilder.Operator.AND;
            return this;
        }

        @Override
        public Begin or() {
            this.negate = false;
            this.operator = ClassPathQueryBuilder.Operator.OR;
            return this;
        }

        @Override
        public LogicalOperations end() {
            this.parent.appendCondition(this.filter);
            this.negate = false;
            return this.parent;
        }

        @Override
        public ClassPathQuery build() {
            if (this.parent != null) {
                throw new InvalidQuerySyntaxException("Query creation must be called after the end of the main begin expression");
            }

            return new PredicateBasedClassPathQuery(this.negate ? this.filter.negate() : this.filter);
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