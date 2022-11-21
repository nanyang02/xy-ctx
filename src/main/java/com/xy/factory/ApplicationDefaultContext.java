package com.xy.factory;


import com.xy.beans.BeanDefine;
import com.xy.context.ApplicationContext;
import com.xy.context.BeanConfigure;
import com.xy.stereotype.Controller;
import com.xy.web.XyDispacher;
import com.xy.web.annotation.EnableWeb;

import java.util.HashSet;
import java.util.Set;

/**
 * bean容器实现类, 提供具体的容器的注册和获取bean的基本功能
 */
public class ApplicationDefaultContext implements ApplicationContext, AutoCloseable {

    private final BeanFactory beanFactory = new BeanFactory(getApplicationContext());

    private final XyDispacher dispacher = new XyDispacher();

    public void webDispatcherJoin() {
        try {
            dispacher.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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

    @Override
    public void close() {
        if (getApplicationContext() != this)
            getApplicationContext().close();
    }

    public void scan(Class<?> c) {
        beanFactory.scan(c);
    }

    public void scan(String c) {
        beanFactory.onlyScan(c);
    }

    // 支持简单的mapping的映射支持，目前还不支持解析参数
    public void useWeb(Class<?> c, Integer port) {
        EnableWeb annotation = c.getAnnotation(EnableWeb.class);
        if (null != annotation) {
            useWeb(port);
        }
    }

    public void useWeb(Integer port) {
        Set<Class<?>> controllerClassList = new HashSet<>();

        for (BeanDefine value : beanFactory.definitions.values()) {
            if (value.getTargetClass().getAnnotation(Controller.class) == null) continue;
            boolean add = controllerClassList.add(value.getTargetClass());
            if (add) {
                dispacher.addMapping(getBean(value.getTargetClass()));
            }
        }

        if (null != port) {
            dispacher.setPort(port);
        }

        // 启动服务
        dispacher.start();
    }

    public void useWeb(Class<?> c) {
        useWeb(c, null);
    }
}
