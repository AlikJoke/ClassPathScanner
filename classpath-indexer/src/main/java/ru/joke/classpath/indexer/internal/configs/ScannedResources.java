package ru.joke.classpath.indexer.internal.configs;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ScannedResources implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Set<String> annotations = new HashSet<>();
    private final Set<String> interfaces = new HashSet<>();
    private final Set<String> classes = new HashSet<>();
    private final Map<String, Set<String>> aliases = new HashMap<>();

    public Set<String> annotations() {
        return annotations;
    }

    public Set<String> interfaces() {
        return interfaces;
    }

    public Set<String> classes() {
        return classes;
    }

    public Map<String, Set<String>> aliases() {
        return aliases;
    }

    public boolean isEmpty() {
        return this.annotations.isEmpty() && this.interfaces.isEmpty() && this.classes.isEmpty() && this.aliases.isEmpty();
    }
}
