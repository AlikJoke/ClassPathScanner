package ru.joke.classpath;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target(value = { CONSTRUCTOR, FIELD, METHOD, MODULE, TYPE, ANNOTATION_TYPE })
public @interface ClassPathIndexed {

    String[] aliases() default {};
}
