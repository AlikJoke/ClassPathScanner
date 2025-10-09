package ru.joke.classpath.indexer.test_util;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class TestJavaFileObject extends SimpleJavaFileObject {

    public TestJavaFileObject(URI uri) {
        super(uri, Kind.OTHER);
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return this.uri.toURL().openStream();
    }
}