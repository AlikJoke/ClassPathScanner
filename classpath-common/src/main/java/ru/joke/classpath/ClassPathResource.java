package ru.joke.classpath;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ClassPathResource {

    String id();

    String name();

    Set<String> aliases();

    String module();

    Set<ClassReference<?>> annotations();

    String packageName();

    Type type();

    interface ClassResource<T> extends ClassPathResource {

        String ID_SEPARATOR = ".";

        @Override
        default String id() {
            return packageName() + ID_SEPARATOR + name();
        }

        Class<T> asClass() throws ClassNotFoundException;

        Set<ClassReference<?>> interfaces();

        Set<ClassReference<?>> superClasses();

        @Override
        default Type type() {
            return Type.CLASS;
        }
    }

    interface ClassMemberResource extends ClassPathResource {

        ClassReference<?> owner();

        String ID_SEPARATOR = "#";

        @Override
        default String id() {
            return packageName() + ID_SEPARATOR + name();
        }
    }

    interface ConstructorResource<T> extends ClassMemberResource {

        List<ClassReference<?>> parameters();

        Constructor<T> asConstructor() throws ClassNotFoundException, NoSuchMethodException;

        @Override
        default Type type() {
            return Type.CONSTRUCTOR;
        }
    }

    interface ModuleResource extends ClassPathResource {
        @Override
        default String id() {
            return name();
        }

        Optional<Module> asModule();

        @Override
        default Type type() {
            return Type.MODULE;
        }
    }

    interface MethodResource extends ClassMemberResource {

        List<ClassReference<?>> parameters();

        Method asMethod() throws ClassNotFoundException, NoSuchMethodException;

        @Override
        default Type type() {
            return Type.METHOD;
        }
    }

    interface FieldResource extends ClassMemberResource {

        Field asField() throws NoSuchFieldException, ClassNotFoundException;

        @Override
        default Type type() {
            return Type.FIELD;
        }
    }

    interface ClassReference<T> {

        String canonicalName();

        Class<T> toClass() throws ClassNotFoundException;
    }

    enum Type {

        MODULE("m"),
        CLASS("c"),
        CONSTRUCTOR("cr"),
        METHOD("md"),
        FIELD("f");

        private final String alias;

        Type(String alias) {
            this.alias = alias;
        }

        public String alias() {
            return this.alias;
        }

        public static Type from(final String alias) {
            for (var type : values()) {
                if (type.alias.equals(alias)) {
                    return type;
                }
            }

            return null;
        }
    }
}
