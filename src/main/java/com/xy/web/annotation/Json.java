package com.xy.web.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Json {
    String value() default "";
    boolean fromBody() default true;
}
