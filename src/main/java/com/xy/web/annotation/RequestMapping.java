package com.xy.web.annotation;

import com.xy.web.MsgType;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

    String value() default "";

    MsgType type() default MsgType.PLAIN;

}