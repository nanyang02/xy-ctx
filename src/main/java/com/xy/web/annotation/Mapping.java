package com.xy.web.annotation;

import com.xy.web.MsgType;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapping {
    String value() default "";

    MsgType type() default MsgType.PLAIN;
}