package com.xy.beans;

/**
 * 提供基于BeanGetter的方式来获取依赖对象. 源于 GreetGo框架.
 *
 * @param <T>
 */
public interface BeanGetter<T> {

    /**
     * 获取实例对象
     *
     * @return
     */
    T get();

    /**
     * 设置对象
     *
     * @param t
     */
    void set(Object t);
}
