module classpath.indexer {
    requires classpath.common;
    requires java.compiler;
    requires jdk.compiler;

    exports ru.joke.classpath.indexer;
    opens ru.joke.classpath.indexer;
}