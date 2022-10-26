package com.xy.ext;

import com.xy.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface DefaultBoolValue {

    boolean value() default false;

}
