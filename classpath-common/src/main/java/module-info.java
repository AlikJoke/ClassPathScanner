import ru.joke.classpath.converters.ClassFieldResourceConverter;
import ru.joke.classpath.converters.ClassResourceConverter;
import ru.joke.classpath.converters.ConcreteClassPathResourceConverter;
import ru.joke.classpath.converters.ModuleResourceConverter;

module classpath.common {
    exports ru.joke.classpath;
    exports ru.joke.classpath.services to classpath.indexer, classpath.scanner;

    uses ConcreteClassPathResourceConverter;
    provides ConcreteClassPathResourceConverter with ClassResourceConverter, ModuleResourceConverter, ClassFieldResourceConverter;
}