package com.xy.web.annotation;

import com.xy.web.ApiContentType;

import java.lang.annotation.*;

/**
 * Class <code>ToJson</code>
 *
 * @author yangnan 2022/11/25 18:19
 * @since 1.8
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Api {
    String label() default "";

    String desc() default "";

    String args() default "";

    String[] kvs() default {};

    ApiContentType apiContentType() default ApiContentType.json;

    Class<?> dtoClass() default void.class;
}
