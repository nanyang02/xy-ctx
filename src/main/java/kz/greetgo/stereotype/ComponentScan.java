package kz.greetgo.stereotype;

import java.lang.annotation.*;

/**
 * 包扫描注解
 * ： 1 直接扫描一些类型得注解使用，这个在spring中，主要是扫组件。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface ComponentScan {
    String value() default "";

    String[] packages() default {};
}