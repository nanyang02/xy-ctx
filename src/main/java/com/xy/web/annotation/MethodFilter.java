package com.xy.web.annotation;

import com.xy.web.RequestMethod;

import java.lang.annotation.*;

/**
 * Class <code>MethodFilter</code>
 *
 * @author yangnan 2023/3/16 15:49
 * @since 1.8
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MethodFilter {
    RequestMethod[] value();
}
