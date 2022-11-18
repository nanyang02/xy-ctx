package com.xy.web.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

    enum Type {
        JSON, PLAIN
    }

    String value() default "";
    Type type() default Type.PLAIN;
}