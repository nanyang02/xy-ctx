package com.xy.web.annotation;

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
}
