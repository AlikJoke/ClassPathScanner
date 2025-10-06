module classpath.common {
    exports ru.joke.classpath;
    exports ru.joke.classpath.services to classpath.indexer, classpath.scanner;
    exports ru.joke.classpath.util to classpath.scanner;
}