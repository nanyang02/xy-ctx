package com.xy.context;

public interface ApplicationContext {

    /**
     * 获取容器上下文对象
     *
     * @return 容器上下文
     */
    ApplicationContext getApplicationContext();

    /**
     * 别名查询容器中的bean对象
     *
     * @param alias 注册使用的别名
     * @param <T>   对象类型
     * @return bean对象
     */
    <T> T getBean(String alias);

    /**
     * 字节码查询容器中的bean对象
     *
     * @param c   bean对象字节码
     * @param <T> 对象类型
     * @return bean对象
     */
    <T> T getBean(Class<T> c);

    /**
     * 注册实例bean对象
     *
     * @param b bean对象
     */
    void regSingleton(Object b);

    /**
     * 注册实例bean对象
     *
     * @param b     bean对象
     * @param alias 注册使用的别名
     */
    void regSingleton(Object b, String alias);

    /**
     * 注册实例bean对象
     *
     * @param b          bean对象
     * @param aliasClass 别名接口类字节码
     */
    void regSingleton(Object b, Class<?> aliasClass);

    /**
     * 注册bean的定义信息
     *
     * @param beanClass bean的字节码
     * @param alias     注册别名
     */
    void regBeanDefinition(Class<?> beanClass, String alias);

    /**
     * 注册bean的定义信息
     *
     * @param beanClass      bean的字节码
     * @param interfaceClass 接口对应的字节码,用于取出类名作为别名找映射的服务对象
     */
    void regBeanDefinition(Class<?> beanClass, Class<?> interfaceClass);

    /**
     * 注册bean的定义信息
     *
     * @param beanClass bean的字节码
     */
    void regBeanDefinition(Class<?> beanClass);
}
