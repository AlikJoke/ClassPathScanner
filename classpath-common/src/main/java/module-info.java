import ru.joke.classpath.converters.internal.ClassFieldResourceConverter;
import ru.joke.classpath.converters.internal.ClassResourceConverter;
import ru.joke.classpath.converters.internal.ConcreteClassPathResourceConverter;
import ru.joke.classpath.converters.internal.ModuleResourceConverter;

module classpath.common {
    exports ru.joke.classpath;
    exports ru.joke.classpath.services to classpath.indexer, classpath.scanner;
    exports ru.joke.classpath.util to classpath.scanner;

    uses ConcreteClassPathResourceConverter;
    provides ConcreteClassPathResourceConverter with ClassResourceConverter, ModuleResourceConverter, ClassFieldResourceConverter;
}