package com.xy.factory;


import com.xy.context.ApplicationContext;
import com.xy.context.BeanConfigure;
import com.xy.web.annotation.EnableWeb;
import com.xy.web.core.WebContext;
import com.xy.web.filter.Filter;

/**
 * bean容器实现类, 提供具体的容器的注册和获取bean的基本功能
 */
public class ApplicationDefaultContext implements ApplicationContext, AutoCloseable {

    private final BeanFactory beanFactory = new BeanFactory(getApplicationContext());

    private final WebContext webContext = new WebContext(this);

    private static boolean useDebug = false;

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setApiCtxPath(String path) {
        if (null != path) {
            webContext.setContextPath(path);
        }
    }

    public void registerFilter(Filter filter) {
        regSingleton(filter);
        webContext.registerFilter(filter);
    }

    public void registerFilter(String beanName) {
        webContext.registerFilter((Filter) getBean(beanName));
    }

    public void registerFilter(Class<?> clazz) {
        webContext.registerFilter((Filter) getBean(clazz));
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

    public void regProxyBean(Object bean) {
        regProxyBean(bean, bean.getClass().getName());
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

    /**
     * 提供支持绑定在指定的端口上运行
     *
     * @param port
     */
    public void useWeb(int port) {
        webContext.setPort(port);
        useWeb();
    }

    public void useWeb(String host, int port) {
        if (null == host) throw new RuntimeException("Host must not empty!");
        webContext.setPort(port);
        webContext.setHost(host);
        useWeb();
    }

    public void useWeb() {
        // web ctx init
        webContext.init();
        // start server thread
        webContext.start();
    }

    public void useWeb(Class<?> c) {
        useWeb(c, null);
    }

    public static boolean enabledDebug() {
        return useDebug;
    }

    public void enableDebugLog(boolean b) {
        useDebug = b;
    }

    public boolean isUseDebug() {
        return useDebug;
    }
}
