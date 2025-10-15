/**
 * Contains the implementation of an annotation processor that indexes classpath resources.
 */
module classpath.indexer {
    requires classpath.common;
    requires java.compiler;

    exports ru.joke.classpath.indexer;
    opens ru.joke.classpath.indexer;
}