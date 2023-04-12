package com.xy.web.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableWeb {
    String value() default "";

    boolean useHotLoad() default false;
}