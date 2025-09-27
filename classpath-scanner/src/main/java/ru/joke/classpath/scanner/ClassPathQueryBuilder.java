package ru.joke.classpath.scanner;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.scanner.impl.ClassPathQueryBuilderImpl;

import java.lang.annotation.Annotation;

public interface ClassPathQueryBuilder {

    Begin begin();

    interface Begin {

        Begin begin();

        LogicalOperations inPackage(String packageName);

        LogicalOperations inModule(String moduleName);

        LogicalOperations annotatedBy(Class<? extends Annotation> annotation);

        LogicalOperations withAlias(String alias);

        LogicalOperations implementsInterface(Class<?> interfaceClass);

        LogicalOperations subClassOf(Class<?> superClass);

        LogicalOperations targetTypes(ClassPathResource.Type... types);

        Begin not();
    }

    interface LogicalOperations extends End {

        Begin and();

        Begin or();
    }

    interface End {

        LogicalOperations end();

        ClassPathQuery build();
    }

    enum Operator {

        OR,

        AND,

        NOT
    }

    static ClassPathQueryBuilder create() {
        return new ClassPathQueryBuilderImpl();
    }
}
