package ru.joke.classpath.scanner;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.scanner.impl.DefaultClassPathScannerBuilder;

import java.lang.annotation.Annotation;

public interface ClassPathScannerBuilder {

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
