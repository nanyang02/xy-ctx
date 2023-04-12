package com.xy.web.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Json {
    // 默认的，是指定一层的属性的子json来转换成对象的key
    String value() default "";

    boolean fromBody() default true;

    // 从formdata中取属性用于做对象的json转换对象
    String fromFormDataParam() default "";
}
