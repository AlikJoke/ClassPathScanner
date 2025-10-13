/**
 * Defines the core model of library.
 */
module classpath.common {
    exports ru.joke.classpath;
    exports ru.joke.classpath.services to classpath.indexer, classpath.scanner;
}