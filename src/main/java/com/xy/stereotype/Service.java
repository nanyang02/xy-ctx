package com.xy.stereotype;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {
    /**
     * 服务的别名定义
     *
     * @return
     */
    String value() default "";

    /**
     * 是否服务上级的服务接口，这个是用于避免出现一个接口对应了多个服务实现，如果是一个接口有多个实现，我们就需要设置成true
     *
     * @return
     */
    boolean ignoreSupper() default false;
}
