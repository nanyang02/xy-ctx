package com.xy.factory;


import com.xy.context.ApplicationContext;
import com.xy.context.BeanConfigure;

/**
 * bean容器实现类, 提供具体的容器的注册和获取bean的基本功能
 */
public class ApplicationDefaultContext implements ApplicationContext {

    private final BeanFactory beanFactory = new BeanFactory(getApplicationContext());

    /**
     * 提供给AppContextAware提供环境对象使用
     *
     * @return 获取到容器上下文
     */
    @Override
    public ApplicationDefaultContext getApplicationContext() {
        return this;
    }

    @Override
    public <T> T getBean(String alias) {
        return beanFactory.getBean(alias);
    }

    @Override
    public <T> T getBean(Class<T> c) {
        return beanFactory.getBean(c);
    }

    @Override
    public void regSingleton(Object b) {
        beanFactory.regSingletonBean(b);
    }

    @Override
    public void regSingleton(Object b, String alias) {
        beanFactory.regSingletonBean(b, alias);
    }

    @Override
    public void regSingleton(Object b, Class<?> aliasClass) {
        beanFactory.regSingletonBean(b, aliasClass.getName());
    }

    public void regBeanConfig(BeanConfigure app) {
        beanFactory.loadConfigBean(app);
    }

    @Override
    public void regBeanDefinition(Class<?> beanClass, String alias) {
        beanFactory.regBeanDefinition(beanClass, alias);
    }

    @Override
    public void regBeanDefinition(Class<?> beanClass, Class<?> interfaceClass) {
        beanFactory.regBeanDefinition(beanClass, interfaceClass);
    }

    @Override
    public void regBeanDefinition(Class<?> beanClass) {
        beanFactory.regBeanDefinition(beanClass);
    }

    public <T> T beanPropInitial(T bean) {
        beanFactory.beanPropInitial(bean);
        return bean;
    }

    public void regProxyBean(Object bean, String alias) {
        beanFactory.regProxyBean(bean, alias);
    }
}
