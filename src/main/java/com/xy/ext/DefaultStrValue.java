package com.xy.ext;

import kz.greetgo.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface DefaultStrValue {
    String value() default "";
}
