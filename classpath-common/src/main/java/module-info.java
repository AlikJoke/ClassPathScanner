import ru.joke.classpath.ClassPathResourceConverter;
import ru.joke.classpath.ClassPathResourcesService;
import ru.joke.classpath.ConcreteClassPathResourceConverter;
import ru.joke.classpath.converters.ClassResourceConverter;
import ru.joke.classpath.converters.DelegateClassPathResourceConverter;
import ru.joke.classpath.converters.FieldResourceConverter;
import ru.joke.classpath.converters.ModuleResourceConverter;
import ru.joke.classpath.services.DefaultSingletonClassPathResourcesService;

module classpath.common {
    exports ru.joke.classpath;
    exports ru.joke.classpath.services;

    uses ClassPathResourcesService;
    provides ClassPathResourcesService with DefaultSingletonClassPathResourcesService;

    uses ClassPathResourceConverter;
    provides ClassPathResourceConverter with DelegateClassPathResourceConverter;

    uses ConcreteClassPathResourceConverter;
    provides ConcreteClassPathResourceConverter with ClassResourceConverter, ModuleResourceConverter, FieldResourceConverter;
}