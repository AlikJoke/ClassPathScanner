import ru.joke.classpath.converters.ClassPathResourceConverter;
import ru.joke.classpath.ClassPathResourcesService;
import ru.joke.classpath.converters.ConcreteClassPathResourceConverter;
import ru.joke.classpath.converters.ClassResourceConverter;
import ru.joke.classpath.converters.DelegateClassPathResourceConverter;
import ru.joke.classpath.converters.ClassFieldResourceConverter;
import ru.joke.classpath.converters.ModuleResourceConverter;
import ru.joke.classpath.services.DefaultClassPathResourcesService;

module classpath.common {
    exports ru.joke.classpath;
    exports ru.joke.classpath.converters;

    uses ClassPathResourcesService;
    provides ClassPathResourcesService with DefaultClassPathResourcesService;

    uses ClassPathResourceConverter;
    provides ClassPathResourceConverter with DelegateClassPathResourceConverter;

    uses ConcreteClassPathResourceConverter;
    provides ConcreteClassPathResourceConverter with ClassResourceConverter, ModuleResourceConverter, ClassFieldResourceConverter;
}